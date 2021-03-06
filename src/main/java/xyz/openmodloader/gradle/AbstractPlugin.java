package xyz.openmodloader.gradle;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import xyz.openmodloader.gradle.util.Constants;

public class AbstractPlugin implements Plugin<Project> {
    protected Project project;

    @Override
    public void apply(Project target) {
        this.project = target;

        // Apply default plugins
        project.apply(ImmutableMap.of("plugin", "java"));
        project.apply(ImmutableMap.of("plugin", "eclipse"));
        project.apply(ImmutableMap.of("plugin", "idea"));

        project.getExtensions().create("minecraft", ModGradleExtension.class);

        // Force add Mojang repository
        addMavenRepo(target, "Mojang", "https://libraries.minecraft.net/");

        // Minecraft libraries configuration
        project.getConfigurations().maybeCreate(Constants.CONFIG_MC_DEPENDENCIES);
        project.getConfigurations().maybeCreate(Constants.CONFIG_MC_DEPENDENCIES_CLIENT);
        project.getConfigurations().maybeCreate(Constants.CONFIG_NATIVES);

        // Common libraries extends from client libraries, CONFIG_MC_DEPENDENCIES will contains all MC dependencies
        project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES).extendsFrom(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES_CLIENT));

        configureIDEs();
        configureCompile();
    }

    /**
     * Permit to create a Task instance of the type in the project
     *
     * @param name The name of the task
     * @param type The type of the task that will be used to create an instance
     * @return The created task object for the project
     */
    public <T extends Task> T makeTask(String name, Class<T> type) {
        return makeTask(project, name, type);
    }

    /**
     * Permit to create a Task instance of the type in a project
     *
     * @param target The target project
     * @param name   The name of the task
     * @param type   The type of the task that will be used to create an instance
     * @return The created task object for the specified project
     */
    public static <T extends Task> T makeTask(Project target, String name, Class<T> type) {
        return target.getTasks().create(name, type);
    }

    /**
     * Permit to add a Maven repository to a target project
     *
     * @param target The garget project
     * @param name   The name of the repository
     * @param url    The URL of the repository
     * @return An object containing the name and the URL of the repository that can be modified later
     */
    public MavenArtifactRepository addMavenRepo(Project target, final String name, final String url) {
        return target.getRepositories().maven(repo -> {
            repo.setName(name);
            repo.setUrl(url);
        });
    }

    /**
     * Add Minecraft dependencies to IDE dependencies
     */
    protected void configureIDEs() {
        // IDEA
        IdeaModel ideaModule = (IdeaModel) project.getExtensions().getByName("idea");

        ideaModule.getModule().getExcludeDirs().addAll(project.files(".gradle", "build", ".idea", "out").getFiles());
        ideaModule.getModule().setDownloadJavadoc(true);
        ideaModule.getModule().setDownloadSources(true);
        ideaModule.getModule().setInheritOutputDirs(true);
        ideaModule.getModule().getScopes().get("COMPILE").get("plus").add(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES));

        // ECLIPSE
        EclipseModel eclipseModule = (EclipseModel) project.getExtensions().getByName("eclipse");
        eclipseModule.getClasspath().getPlusConfigurations().add(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES));
    }

    /**
     * Add Minecraft dependencies to compile time
     */
    protected void configureCompile() {
        JavaPluginConvention javaModule = (JavaPluginConvention) project.getConvention().getPlugins().get("java");

        SourceSet main = javaModule.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSet test = javaModule.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

        main.setCompileClasspath(main.getCompileClasspath().plus(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES)));
        test.setCompileClasspath(test.getCompileClasspath().plus(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES)));
        main.setRuntimeClasspath(main.getCompileClasspath().plus(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES)));
        test.setCompileClasspath(test.getCompileClasspath().plus(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES)));

        Javadoc javadoc = (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);
        javadoc.setClasspath(main.getOutput().plus(main.getCompileClasspath()));
    }
}
