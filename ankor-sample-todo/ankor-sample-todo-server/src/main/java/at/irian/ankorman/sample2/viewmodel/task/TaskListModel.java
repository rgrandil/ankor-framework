package at.irian.ankorman.sample2.viewmodel.task;

import at.irian.ankor.annotation.ActionListener;
import at.irian.ankor.annotation.ChangeListener;
import at.irian.ankor.annotation.Param;
import at.irian.ankor.ref.Ref;
import at.irian.ankor.viewmodel.ViewModelBase;
import at.irian.ankor.viewmodel.ViewModelProperty;
import at.irian.ankorman.sample2.domain.task.Task;
import at.irian.ankorman.sample2.server.TaskRepository;
import at.irian.ankorman.sample2.viewmodel.task.helper.Data;
import at.irian.ankorman.sample2.viewmodel.task.helper.Paginator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class TaskListModel extends ViewModelBase {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TaskListModel.class);

    @JsonIgnore
    private final TaskRepository taskRepository;

    private ViewModelProperty<String> filter; // XXX: Not possible to use enum type Filter -> type errors again
    private ViewModelProperty<Integer> itemsLeft;

    // TODO: Use a list instead of Data<Task>
    private Data<Task> tasks;

    // XXX: Did not work using a list
    // private List<Task> tasks = new ArrayList<Task>();

    public TaskListModel(Ref viewModelRef, TaskRepository taskRepository) {
        super(viewModelRef);

        this.taskRepository = taskRepository;

        // this.filter = new ViewModelProperty<>(viewModelRef, "filter", Filter.all.toString());
        this.filter.set(Filter.all.toString());
        this.itemsLeft.set(taskRepository.getTasks().size()); // X-X-X: Weird type error if not using string
        this.tasks = fetchTasksData(new Paginator(0, Integer.MAX_VALUE));
    }

    @ChangeListener(pattern = {
            "**.<TaskListModel>.itemsLeft",
            "**.<TaskListModel>.filter"
    })
    public void reloadTasks() {
        LOG.info("reloading tasks");
        Data<Task> tasksData = fetchTasksData(tasks.getPaginator());
        thisRef().append("tasks").setValue(tasksData);
    }

    @ActionListener(name = "newTodo")
    public void newTodo(@Param("title") final String title) {
        LOG.info("Add new task to task repository");

        Task task = new Task(title);
        taskRepository.saveTask(task);

        // X-X-X: Setting values to refs here causes (sometimes!?) exceptions -> should be fixed in newer ankor commit

        thisRef().append("itemsLeft").setValue(itemsLeft.get() + 1);

        // X-X-X: Difference? -> Convenience
        // itemsLeft.set(String.valueOf(currItemsLeft + 1));
    }

    private Data<Task> fetchTasksData(Paginator paginator) {
        paginator.reset();
        Filter filterEnum = Filter.valueOf(filter.get());
        return taskRepository.searchTasks(filterEnum, paginator.getFirst(), paginator.getMaxResults());
    }

    public ViewModelProperty<Integer> getItemsLeft() {
        return itemsLeft;
    }

    public void setItemsLeft(ViewModelProperty<Integer> itemsLeft) {
        this.itemsLeft = itemsLeft;
    }

    public Data<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Data<Task> tasks) {
        this.tasks = tasks;
    }

    public ViewModelProperty<String> getFilter() {
        return filter;
    }

    public void setFilter(ViewModelProperty<String> filter) {
        this.filter = filter;
    }
}
