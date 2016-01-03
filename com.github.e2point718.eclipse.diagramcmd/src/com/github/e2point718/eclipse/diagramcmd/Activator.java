package com.github.e2point718.eclipse.diagramcmd;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	//A sample key
	private static final String DIAGRAM_FILE_KEY ="-com.github.e2point718.eclipse.diagramcmd=";
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		String diagramFile = null;
		for(String arg:Platform.getCommandLineArgs()){
			if(arg.startsWith(DIAGRAM_FILE_KEY)){
				diagramFile = arg.substring(DIAGRAM_FILE_KEY.length()).trim();
			}
		}
		if(diagramFile!=null){
			System.out.println("Loading diagram file "+diagramFile);
			java.io.File f = new java.io.File(diagramFile);
			if(f.exists()){
				IFile ifile = getIFile(f);
				if(ifile!=null){
					System.out.println("Loading workspace copy "+ifile.getFullPath().toString());
					Diagram diagram = loadGraphitiDiagram(ifile);
					System.out.println("Loaded diagram "+diagram);
				}
			} else {
				System.err.println("Cannot access "+f.getAbsolutePath());
			}
		}
	}
	
	private Diagram loadGraphitiDiagram(IFile file){
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		URI fileUri = rs.getURIConverter().normalize(URI.createPlatformResourceURI(file.getFullPath().toString(), true));
		TransactionalEditingDomain ed = TransactionalEditingDomain.Factory.INSTANCE.createEditingDomain(rs);
		ed.getResourceSet().getResource(fileUri, true);
		EObject eObject = ed.getResourceSet().getEObject(fileUri.appendFragment("/0"), false);
		return (Diagram)eObject;
	}
	
	/**
	 * Temporary implementation - TODO right way to access OS files from OSGI?
	 * This creates a project named test in the workspace and copies the OS file into it
	 * This shouldn't be the way to access OS files in OSGI
	 * @param os external file
	 * @return workspace file
	 * @throws CoreException
	 * @throws FileNotFoundException 
	 */
	private IFile getIFile(java.io.File f) throws CoreException, FileNotFoundException {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		System.out.println("Using workspace "+workspace.getRoot().getLocation().toString());
		IProject project = workspace.getRoot().getProject("test");//temporary
		IFile file = null;
    	if(!project.exists()){
    		project.create(null);        		
    	}
    	project.open(null);
		file = project.getFile(f.getName());
		if(!file.exists()){
			file.create(new FileInputStream(f), true, null);
		}   
    	return file;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		
	}

}
