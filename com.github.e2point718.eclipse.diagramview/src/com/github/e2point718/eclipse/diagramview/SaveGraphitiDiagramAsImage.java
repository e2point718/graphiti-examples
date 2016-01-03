package com.github.e2point718.eclipse.diagramview;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;

public class SaveGraphitiDiagramAsImage extends Action {

	private final IWorkbenchWindow window;
	public SaveGraphitiDiagramAsImage(IWorkbenchWindow window) {
		super("Export Graphiti Diagram File to Image");
		this.window = window;
		setId("com.github.e2point718.eclipse.diagramview.SaveGraphitiDiagramAsImage");
	}

	@Override
	public void run() {
		String fileName = getFile("Open Graphiti Diagram File","*.diagram");
		if(fileName!=null){
			try {
				java.io.File diagFile = new java.io.File(fileName);
				IFile file = createTemporaryFile(diagFile);
				saveImage(diagFile,file);
			} catch (Exception e) {
				e.printStackTrace();
				ErrorDialog.openError(Display.getCurrent().getActiveShell(),
						"err", e.getMessage(), Status.OK_STATUS);
			}
		}
	}
	
	private String getFile(String title,String extension){
		FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] { extension});
		dialog.setFilterIndex(0);
		dialog.setText(title);
		return dialog.open();
	}
	
	private void saveImage(java.io.File diagFile,IFile file)throws Exception{
		ResourceSet rs = new ResourceSetImpl();
		URI fileUri = rs.getURIConverter().normalize(URI.createPlatformResourceURI(file.getFullPath().toString(), true));
		URI graphitiUri = fileUri.appendFragment("/0");
		TransactionalEditingDomain edDom = TransactionUtil.getEditingDomain(rs);
		if (edDom == null) {
			edDom = TransactionalEditingDomain.Factory.INSTANCE.createEditingDomain(rs);
		}
		Resource emfRs = edDom.getResourceSet().getResource(fileUri, false);
		if (emfRs == null) {
			emfRs = edDom.getResourceSet().getResource(fileUri, true);
		}
		Diagram diagram = (Diagram)edDom.getResourceSet().getEObject(graphitiUri, false);		
		byte[] bytes = GraphitiUi.getImageService().convertDiagramToBytes(diagram, SWT.IMAGE_JPEG);
		java.io.File outFile = new java.io.File(diagFile.getParentFile(),diagFile.getName()+".jpg");
		FileOutputStream fos = new FileOutputStream(outFile);
		try{
			fos.write(bytes);
		}finally{
			fos.close();
		}
		MessageDialog.openInformation(window.getShell(), "Export successful", "Graphiti diagram was saved to "+outFile.getAbsolutePath());
	}
	
	private IFile createTemporaryFile(java.io.File osFile) throws Exception {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("test");//temporary
		IFile file = null;
    	if(!project.exists()){
    		project.create(null);        		
    	}
    	project.open(null);
		file = project.getFile(osFile.getName());
		if(!file.exists()){
			file.create(new FileInputStream(osFile), true, null);
		}  
		return file;
	}
	
	
}
