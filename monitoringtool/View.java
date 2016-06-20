package monitoringtool;

public interface View {
    /**
     * When called, the implementing view should call all information from the model and present them.
     */
    public void update();
}
