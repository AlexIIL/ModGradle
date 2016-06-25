package xyz.openmodloader.gradle;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

/**
 * Base of all sub Gradle plugin of OML
 * Created by Thog the 25/06/2016
 */
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
    }

    /**
     * Permite to create a Task instance of the type in the project
     * @param name The name of the task
     * @param type The type of the task that will be used to create an instance
     * @return The created task object for the project
     */
    public <T extends Task> T makeTask(String name, Class<T> type)
    {
        return makeTask(project, name, type);
    }

    /**
     * Permite to create a Task instance of the type in a project
     * @param target The target project
     * @param name The name of the task
     * @param type The type of the task that will be used to create an instance
     * @return The created task object for the specified project
     */
    public static <T extends Task> T makeTask(Project target, String name, Class<T> type)
    {
        return target.getTasks().create(name, type);
    }
}
