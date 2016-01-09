package io.pictura.servlet;

import io.pictura.servlet.annotation.ResourcePath;
import io.pictura.servlet.FileResourceLocator;
import java.lang.reflect.Field;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class ResourcePathTest {
    
    @Test
    public void testFileResourceLocator() throws Exception {
	TestLocator l = new TestLocator();
	
	Field rootPath = l.getClass().getSuperclass().getDeclaredField("rootPath");
	rootPath.setAccessible(true);		
	
	assertEquals("/tmp/images", (String)rootPath.get(l));
    }
    
    @ResourcePath("/tmp/images")
    private static class TestLocator extends FileResourceLocator {	
    }
    
}
