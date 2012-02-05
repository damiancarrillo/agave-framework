package co.cdev.agave.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.objectweb.asm.ClassReader;

import co.cdev.agave.AgaveConfigurationException;

public class ConfigGeneratorImpl implements ConfigGenerator {

    @Override
    public Config scanClassesWithinRootDirectory(File rootDirectory) 
            throws FileNotFoundException, IOException, ClassNotFoundException, AgaveConfigurationException {
        return scanClassesWithinRootDirectory(getClass().getClassLoader(), rootDirectory);
    }
    
    @Override
    public Config scanClassesWithinRootDirectory(ClassLoader classLoader, File rootDirectory) 
            throws FileNotFoundException, IOException, ClassNotFoundException, AgaveConfigurationException {
        Config config = new ConfigImpl();
        scanForHandlers(classLoader, rootDirectory, config);
        return config;
    }
    
    private void scanForHandlers(ClassLoader classLoader, File rootDirectory, Config config)
            throws FileNotFoundException, IOException, ClassNotFoundException, AgaveConfigurationException {
        if (rootDirectory != null && rootDirectory.isDirectory() && rootDirectory.canRead()) {
            for (File node : rootDirectory.listFiles()) {
                if (node.isDirectory()) {
                    scanForHandlers(classLoader, node, config);
                } else if (node.isFile() && node.getName().endsWith(".class")) {
                    FileInputStream nodeIn = new FileInputStream(node);
                    
                    try {
                        ClassReader classReader = new ClassReader(nodeIn);
                        Collection<ScanResult> scanResults = new ArrayList<ScanResult>();
                        classReader.accept(new HandlerScanner(scanResults), ClassReader.SKIP_CODE);

                        for (ScanResult scanResult : scanResults) {
                            HandlerDescriptor descriptor = new HandlerDescriptorImpl(classLoader, scanResult);
                            descriptor.locateAnnotatedHandlerMethods(scanResult);
                            config.addHandlerDescriptor(descriptor);
                        }
                    } finally {
                        nodeIn.close();
                    }
                }
            }
        }
    }
    
}
