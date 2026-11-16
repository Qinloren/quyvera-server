package com.zeeyeh.quyvera.plugin.service.plugin.library;

import com.zeeyeh.quyvera.plugin.sdk.exceptions.InvalidPluginException;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.repository.supplier.RepositorySystemSupplier;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Maven依赖解析器
 * @author Qinloren
 * @date 2026/11/15 13:16
 */
public class MavenLibraryResolver implements PluginLibraryResolver {
    private static final LibraryRepository MAVEN_CENTRAL = new LibraryRepository("central", URI.create("https://repo.maven.apache.org/maven2/"));
    private final RepositorySystem repositorySystem;
    private final Path localRepository;
    private final List<RemoteRepository> remoteRepositories;

    public MavenLibraryResolver(Path localRepository) {
        this(localRepository, List.of(MAVEN_CENTRAL));
    }

    public MavenLibraryResolver(Path localRepository, List<LibraryRepository> repositories) {
        Objects.requireNonNull(localRepository, "Local repository cannot be null");
        Objects.requireNonNull(repositories, "Repositories cannot be null");
        try {
            Path normalized = localRepository.toAbsolutePath().normalize();
            Files.createDirectories(normalized);
            this.localRepository = normalized.toRealPath();
        } catch (IOException e) {
            throw new InvalidPluginException("Cannot initialize local Maven repository: %s".formatted(localRepository), e);
        }
        this.repositorySystem = createRepositorySystem();
        this.remoteRepositories = repositories.stream()
                .map(repository -> new RemoteRepository.Builder(
                        repository.id(),
                        "default",
                        repository.url().toString()
                ).build()).toList();
    }

    @Override
    public List<URL> resolve(String pluginName, List<String> libraries) {
        if (StringUtils.isBlank(pluginName)) {
            throw new IllegalArgumentException("Plugin name cannot be empty");
        }
        Objects.requireNonNull(libraries, "Libraries cannot be null");
        if (libraries.isEmpty()) {
            return List.of();
        }
        try {
            LinkedHashSet<LibraryCoordinate> coordinates = new LinkedHashSet<>();
            for (String library : libraries) {
                coordinates.add(LibraryCoordinate.parse(library));
            }
            DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
            LocalRepository localRepo = new LocalRepository(localRepository.toFile());
            session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo));
            List<Dependency> dependencies = new ArrayList<>();
            for (LibraryCoordinate coordinate : coordinates) {
                DefaultArtifact artifact = new DefaultArtifact(coordinate.groupId(), coordinate.artifactId(), "", "jar", coordinate.version());
                dependencies.add(new Dependency(artifact, JavaScopes.RUNTIME));
            }
            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setDependencies(dependencies);
            collectRequest.setRepositories(remoteRepositories);
            DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME));
            DependencyResult dependencyResult = repositorySystem.resolveDependencies(session, dependencyRequest);
            LinkedHashSet<Path> jarPaths = new LinkedHashSet<>();
            for (ArtifactResult artifactResult : dependencyResult.getArtifactResults()) {
                Artifact artifact = artifactResult.getArtifact();
                if (!"jar".equals(artifact.getExtension())) {
                    continue;
                }
                if (artifact.getFile() == null) {
                    throw new InvalidPluginException("Resolved artifact has not file: %s".formatted(artifact));
                }
                Path jarPath = artifact.getFile().toPath();
                if (!Files.isRegularFile(jarPath)) {
                    throw new InvalidPluginException("Resolved JAR does not exist: %s".formatted(jarPath));
                }
                jarPaths.add(jarPath.toRealPath());
            }
            List<URL> urls = new ArrayList<>();
            for (Path jarPath : jarPaths) {
                urls.add(jarPath.toUri().toURL());
            }
            return List.copyOf(urls);
        } catch (DependencyResolutionException e) {
            throw new InvalidPluginException("Failed to resolve libraries for plugin: %s".formatted(pluginName), e);
        } catch (IOException | IllegalArgumentException e) {
            throw new InvalidPluginException("Failed to process libraries for plugin: %s".formatted(pluginName), e);
        }
    }

    private static RepositorySystem createRepositorySystem() {
        return new RepositorySystemSupplier().get();
    }
}
