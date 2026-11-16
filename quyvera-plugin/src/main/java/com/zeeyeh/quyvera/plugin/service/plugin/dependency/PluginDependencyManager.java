package com.zeeyeh.quyvera.plugin.service.plugin.dependency;

import com.zeeyeh.quyvera.plugin.sdk.entity.PluginMeta;
import com.zeeyeh.quyvera.plugin.sdk.exceptions.InvalidPluginException;
import com.zeeyeh.quyvera.plugin.service.plugin.loader.PluginClassLoader;

import java.util.*;

/**
 * 插件依赖管理器。AI生成
 * <p>
 * 负责：
 * 1. 检查插件名称重复。
 * 2. 检查强依赖是否存在。
 * 3. 忽略不存在的软依赖。
 * 4. 检测强依赖循环。
 * 5. 在不破坏强依赖的前提下处理软依赖循环。
 * 6. 生成稳定的拓扑加载顺序。
 * 7. 生成每个插件的直接依赖列表。
 * </p>
 * 不负责：
 * 1. 扫描插件目录。
 * 2. 解析 YAML。
 * 3. 创建 ClassLoader。
 * 4. 加载插件主类。
 * 5. 执行插件生命周期。
 * @author Qinloren
 * @date 2026/11/15 11:29
 */
public final class PluginDependencyManager {

    /**
     * 依赖解析结果。
     * <p>
     * loadOrder:
     *     插件加载顺序。
     * <p>
     * dependencies:
     *     插件名称 -> 该插件可委派的直接依赖名称。
     */
    public record Plan(
            List<String> loadOrder,
            Map<String, List<String>> dependencies
    ) {

        public Plan {

            loadOrder = List.copyOf(loadOrder);

            Map<String, List<String>> copied =
                    new LinkedHashMap<>();

            dependencies.forEach(
                    (name, values) ->
                            copied.put(name, List.copyOf(values))
            );

            dependencies = Collections.unmodifiableMap(copied);
        }

        public List<String> dependenciesOf(String pluginName) {

            return dependencies.getOrDefault(
                    pluginName,
                    List.of()
            );
        }
    }

    /**
     * 解析插件依赖关系。
     * <p>
     * 输入列表的顺序会作为稳定排序的初始顺序。
     */
    public Plan resolve(
            List<PluginMeta> descriptions
    ) {

        Objects.requireNonNull(
                descriptions,
                "descriptions"
        );

        // 保留插件扫描顺序。
        Map<String, PluginMeta> plugins =
                new LinkedHashMap<>();

        Map<String, Integer> positions =
                new HashMap<>();

        for (int i = 0; i < descriptions.size(); i++) {

            PluginMeta description =
                    Objects.requireNonNull(
                            descriptions.get(i),
                            "plugin description"
                    );

            String name = description.getName();

            if (plugins.putIfAbsent(name, description) != null) {
                throw new InvalidPluginException(
                        "Duplicate plugin name: " + name
                );
            }

            positions.put(name, i);
        }

        // 图的方向：dependency -> dependent。
        Map<String, LinkedHashSet<String>> graph =
                createGraph(plugins.keySet());

        // 保存最终的直接依赖关系。
        Map<String, LinkedHashSet<String>> effectiveDependencies =
                new LinkedHashMap<>();

        for (String name : plugins.keySet()) {
            effectiveDependencies.put(
                    name,
                    new LinkedHashSet<>()
            );
        }

        // 先添加强依赖。
        addHardDependencies(
                plugins,
                graph,
                effectiveDependencies
        );

        // 强依赖必须无环。
        List<String> hardCycle = findCycle(graph);

        if (!hardCycle.isEmpty()) {
            throw new InvalidPluginException(
                    "Hard dependency cycle detected: "
                            + String.join(" -> ", hardCycle)
            );
        }

        // 再按插件扫描顺序添加软依赖。
        addSoftDependencies(
                plugins,
                graph,
                effectiveDependencies
        );

        // 所有有效依赖边确定后，执行稳定拓扑排序。
        List<String> loadOrder = topologicalSort(
                graph,
                positions
        );

        return new Plan(
                loadOrder,
                toImmutableLists(effectiveDependencies)
        );
    }

    /**
     * 创建空图。
     */
    private Map<String, LinkedHashSet<String>> createGraph(
            Set<String> pluginNames
    ) {

        Map<String, LinkedHashSet<String>> graph =
                new LinkedHashMap<>();

        for (String name : pluginNames) {
            graph.put(name, new LinkedHashSet<>());
        }

        return graph;
    }

    /**
     * 添加强依赖。
     * <p>
     * A depend B:
     *     B -> A
     */
    private void addHardDependencies(
            Map<String, PluginMeta> plugins,
            Map<String, LinkedHashSet<String>> graph,
            Map<String, LinkedHashSet<String>> effectiveDependencies
    ) {

        for (PluginMeta description : plugins.values()) {

            String pluginName = description.getName();

            Set<String> unique = new HashSet<>();

            for (String dependencyName : description.getDepend()) {

                if (!unique.add(dependencyName)) {
                    throw new InvalidPluginException(
                            "Duplicate hard dependency "
                                    + dependencyName
                                    + " in plugin "
                                    + pluginName
                    );
                }

                if (pluginName.equals(dependencyName)) {
                    throw new InvalidPluginException(
                            "Plugin cannot depend on itself: "
                                    + pluginName
                    );
                }

                if (!plugins.containsKey(dependencyName)) {
                    throw new InvalidPluginException(
                            "Missing hard dependency "
                                    + dependencyName
                                    + " required by "
                                    + pluginName
                    );
                }

                graph.get(dependencyName).add(pluginName);

                effectiveDependencies
                        .get(pluginName)
                        .add(dependencyName);
            }
        }
    }

    /**
     * 添加软依赖。
     * <p>
     * 不存在的软依赖直接忽略。
     * <p>
     * 如果添加某条软依赖边会造成循环，
     * 则忽略该软依赖边。
     * <p>
     * 注意：
     * 被忽略的软依赖也不会进入最终 ClassLoader
     * 的直接依赖列表，避免类委派再次形成环。
     */
    private void addSoftDependencies(
            Map<String, PluginMeta> plugins,
            Map<String, LinkedHashSet<String>> graph,
            Map<String, LinkedHashSet<String>> effectiveDependencies
    ) {

        for (PluginMeta description : plugins.values()) {

            String pluginName = description.getName();

            Set<String> unique = new HashSet<String>(
                    description.getDepend()
            );

            for (String dependencyName : description.getSoftDepend()) {

                if (!unique.add(dependencyName)) {
                    throw new InvalidPluginException(
                            "Duplicate or conflicting dependency "
                                    + dependencyName
                                    + " in plugin "
                                    + pluginName
                    );
                }

                if (pluginName.equals(dependencyName)) {
                    throw new InvalidPluginException(
                            "Plugin cannot softdepend on itself: "
                                    + pluginName
                    );
                }

                // 软依赖不存在时允许继续。
                if (!plugins.containsKey(dependencyName)) {
                    continue;
                }

                /*
                 * 新边为：
                 * dependencyName -> pluginName
                 *
                 * 如果当前已经存在：
                 * pluginName -> ... -> dependencyName
                 *
                 * 那么添加后会产生循环。
                 */
                if (hasPath(
                        graph,
                        pluginName,
                        dependencyName
                )) {
                    continue;
                }

                graph.get(dependencyName).add(pluginName);

                effectiveDependencies
                        .get(pluginName)
                        .add(dependencyName);
            }
        }
    }

    /**
     * 判断 graph 中是否存在 start -> ... -> target。
     */
    private boolean hasPath(
            Map<String, LinkedHashSet<String>> graph,
            String start,
            String target
    ) {

        Deque<String> stack = new ArrayDeque<>();

        Set<String> visited = new HashSet<>();

        stack.push(start);

        while (!stack.isEmpty()) {

            String current = stack.pop();

            if (!visited.add(current)) {
                continue;
            }

            if (current.equals(target)) {
                return true;
            }

            for (String next : graph.getOrDefault(
                    current,
                    new LinkedHashSet<>()
            )) {
                stack.push(next);
            }
        }

        return false;
    }

    /**
     * 查找图中的一个循环路径。
     * <p>
     * 返回空列表表示无环。
     */
    private List<String> findCycle(
            Map<String, LinkedHashSet<String>> graph
    ) {

        Set<String> visited = new HashSet<>();

        Set<String> visiting = new HashSet<>();

        Deque<String> path = new ArrayDeque<>();

        for (String name : graph.keySet()) {

            List<String> cycle = findCycleDfs(
                    name,
                    graph,
                    visited,
                    visiting,
                    path
            );

            if (!cycle.isEmpty()) {
                return cycle;
            }
        }

        return List.of();
    }

    private List<String> findCycleDfs(
            String current,
            Map<String, LinkedHashSet<String>> graph,
            Set<String> visited,
            Set<String> visiting,
            Deque<String> path
    ) {

        if (visiting.contains(current)) {

            List<String> cycle = new ArrayList<>();

            boolean collecting = false;

            for (String name : path) {

                if (name.equals(current)) {
                    collecting = true;
                }

                if (collecting) {
                    cycle.add(name);
                }
            }

            cycle.add(current);

            return cycle;
        }

        if (!visited.add(current)) {
            return List.of();
        }

        visiting.add(current);
        path.addLast(current);

        for (String next : graph.get(current)) {

            List<String> cycle = findCycleDfs(
                    next,
                    graph,
                    visited,
                    visiting,
                    path
            );

            if (!cycle.isEmpty()) {
                return cycle;
            }
        }

        path.removeLast();
        visiting.remove(current);

        return List.of();
    }

    /**
     * 稳定拓扑排序。
     * <p>
     * 入度为 0 的节点按照原始扫描顺序出队。
     */
    private List<String> topologicalSort(
            Map<String, LinkedHashSet<String>> graph,
            Map<String, Integer> positions
    ) {

        Map<String, Integer> indegree = new HashMap<>();

        for (String name : graph.keySet()) {
            indegree.put(name, 0);
        }

        for (Set<String> dependents : graph.values()) {

            for (String dependent : dependents) {
                indegree.merge(dependent, 1, Integer::sum);
//                indegree.compute(
//                        dependent,
//                        (key, value) -> value + 1
//                );
            }
        }

        PriorityQueue<String> ready =
                new PriorityQueue<>(
                        Comparator.comparingInt(positions::get)
                );

        for (Map.Entry<String, Integer> entry :
                indegree.entrySet()) {

            if (entry.getValue() == 0) {
                ready.add(entry.getKey());
            }
        }

        List<String> result = new ArrayList<>();

        while (!ready.isEmpty()) {

            String current = ready.poll();

            result.add(current);

            for (String dependent : graph.get(current)) {

                int remaining = indegree.merge(dependent, -1, Integer::sum);
//                int remaining = indegree.compute(
//                        dependent,
//                        (key, value) -> value - 1
//                );

                if (remaining == 0) {
                    ready.add(dependent);
                }
            }
        }

        if (result.size() != graph.size()) {
            throw new InvalidPluginException(
                    "Plugin dependency graph contains a cycle"
            );
        }

        return List.copyOf(result);
    }

    /**
     * 将依赖集合转换为不可变 List。
     */
    private Map<String, List<String>> toImmutableLists(
            Map<String, LinkedHashSet<String>> source
    ) {

        Map<String, List<String>> result =
                new LinkedHashMap<>();

        source.forEach(
                (name, dependencies) ->
                        result.put(
                                name,
                                List.copyOf(dependencies)
                        )
        );

        return result;
    }

    /**
     * 根据 Plan 初始化所有 ClassLoader 的依赖。
     * <p>
     * 前置条件：
     * 1. 所有插件 ClassLoader 已经创建。
     * 2. 所有 ClassLoader 尚未加载插件主类。
     * 3. loaders 中的键与插件 name 一致。
     * <p>
     * 此方法先验证全部映射，再执行初始化。
     */
    public void initializeClassLoaders(
            Plan plan,
            Map<String, PluginClassLoader> loaders
    ) {

        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(loaders, "loaders");

        // 先检查所有插件都具有对应 ClassLoader。
        for (String pluginName : plan.loadOrder()) {

            if (!loaders.containsKey(pluginName)) {
                throw new InvalidPluginException(
                        "Missing ClassLoader for plugin: "
                                + pluginName
                );
            }

            if (loaders.get(pluginName) == null) {
                throw new InvalidPluginException(
                        "Null ClassLoader for plugin: "
                                + pluginName
                );
            }
        }

        // 再检查所有依赖映射。
        for (String pluginName : plan.loadOrder()) {

            for (String dependencyName :
                    plan.dependenciesOf(pluginName)) {

                if (!loaders.containsKey(dependencyName)) {
                    throw new InvalidPluginException(
                            "Missing dependency ClassLoader: "
                                    + dependencyName
                    );
                }
            }
        }

        // 所有 ClassLoader 已经存在后，再初始化依赖。
        for (String pluginName : plan.loadOrder()) {

            PluginClassLoader loader = loaders.get(pluginName);

            List<PluginClassLoader> dependencies =
                    plan.dependenciesOf(pluginName)
                            .stream()
                            .map(loaders::get)
                            .toList();

            loader.initializeDependencies(dependencies);
        }
    }
}
