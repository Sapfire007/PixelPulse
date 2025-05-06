import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;
import javax.swing.JComponent;

/**
 * A handler for drag and drop file operations
 */
public class FileDropHandler implements DropTargetListener {
    private JComponent component;
    private FileDropListener listener;
    private DropTarget dropTarget;
    
    /**
     * Interface for file drop events
     */
    public interface FileDropListener {
        void filesDropped(List<File> files);
    }
    
    /**
     * Create a new file drop handler
     * @param component The component to enable drag and drop on
     * @param listener The listener to notify when files are dropped
     */
    public FileDropHandler(JComponent component, FileDropListener listener) {
        this.component = component;
        this.listener = listener;
        this.dropTarget = new DropTarget(component, this);
    }
    
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (isDragAcceptable(dtde)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
            dtde.rejectDrag();
        }
    }
    
    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        if (isDragAcceptable(dtde)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
            dtde.rejectDrag();
        }
    }
    
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        if (isDragAcceptable(dtde)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
            dtde.rejectDrag();
        }
    }
    
    @Override
    public void dragExit(DropTargetEvent dte) {
        // Do nothing
    }
    
    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                
                @SuppressWarnings("unchecked")
                List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                
                if (listener != null) {
                    listener.filesDropped(fileList);
                }
                
                dtde.dropComplete(true);
                return;
            }
            
            dtde.rejectDrop();
        } catch (Exception e) {
            e.printStackTrace();
            dtde.rejectDrop();
        }
    }
    
    /**
     * Check if the drag event contains acceptable data
     */
    private boolean isDragAcceptable(DropTargetDragEvent dtde) {
        return dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }
    
    /**
     * Remove the drop target from the component
     */
    public void remove() {
        if (dropTarget != null) {
            dropTarget.removeDropTargetListener(this);
        }
    }
}
