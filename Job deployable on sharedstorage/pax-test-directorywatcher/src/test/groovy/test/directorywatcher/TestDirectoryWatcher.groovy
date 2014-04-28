package test.directorywatcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.junit.runner.JUnitCore;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.wiperdog.directorywatcher.Listener;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestDirectoryWatcher {
	public TestDirectoryWatcher() {
	}

	@Inject
	private org.osgi.framework.BundleContext context;
	@Configuration
	public Option[] config() {

		return options(
		cleanCaches(true),
		frameworkStartLevel(6),
		// felix log level
		systemProperty("felix.log.level").value("4"), // 4 = DEBUG
		// setup properties for fileinstall bundle.
		systemProperty("felix.home").value(System.getProperty("user.dir")),
		// Pax-exam make this test code into OSGi bundle at runtime, so
		// we need "groovy-all" bundle to use this groovy test code.
		mavenBundle("org.codehaus.groovy", "groovy-all", "2.2.1").startLevel(2),
		wrappedBundle(mavenBundle("jcifs", "jcifs", "1.3.17")),		
		mavenBundle("org.wiperdog", "org.wiperdog.directorywatcher", "0.1.0").startLevel(2),
		junitBundles()
		);
	}
	@Before
	public void prepare() {
	}

	@After
	public void finish() {
	}

	@Test
	public void Test() {
		String user = "luongnx";
		String pass = "luong123";
		String host = "10.0.0.223";
		String jobFolder =  "luongnx/var/job/";
		// Folder need end with "/"
		String path = "smb://" + host + "/" + jobFolder;
		
		//Watching for folder at local machine
		JobListener jl = new JobListener("/home/luongnx/wdHome3/var/job");		
		context.registerService(Listener.class.getName(),jl, null)
		
		//Watching for folder located on via network
		JobListener jl2 = new JobListener(jobFolder,host,user,pass);
		context.registerService(Listener.class.getName(),jl2, null)
		Thread.sleep(600000)
	}


}
