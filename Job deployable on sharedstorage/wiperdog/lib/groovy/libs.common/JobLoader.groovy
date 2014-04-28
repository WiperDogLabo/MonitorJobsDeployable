// job loader

import org.wiperdog.directorywatcher.Listener
import org.osgi.framework.ServiceReference
import org.osgi.util.tracker.ServiceTracker
import org.osgi.util.tracker.ServiceTrackerCustomizer
import org.wiperdog.jobmanager.JobFacade
import org.apache.log4j.Logger
import org.osgi.framework.BundleContext
import org.osgi.framework.FrameworkUtil
import jcifs.smb.SmbFile

/**
 * job 専用loader
 * (未実装）
 * job の記述が簡潔になるよう、いろいろコチラ側で処理してあげる。
 *  IST_HOME/var/jobs/*.job を監視してロードする。
 */
class JobLoader implements Listener, ServiceTrackerCustomizer {
	def shell
	def dir
	def interval
	def context
	def trackerObj
	def jobdsl
	def jobfacade
	def properties
	def shared_host
	def shared_user
	def shared_pass

	public JobLoader(BundleContext ctx, GroovyShell shell) {
		this.shell = shell
		this.context = ctx
		MonitorJobConfigLoader configLoader = new MonitorJobConfigLoader(context)
		properties = configLoader.getProperties();
		if (properties.get(ResourceConstants.SHARED_HOST) == null || properties.get(ResourceConstants.SHARED_HOST) == "") {
			dir = properties.get(ResourceConstants.JOB_DIRECTORY)
		} else {
			shared_host = properties.get(ResourceConstants.SHARED_HOST)
			shared_user = properties.get(ResourceConstants.SHARED_USER)
			shared_pass = properties.get(ResourceConstants.SHARED_PASS)
			dir = properties.get(ResourceConstants.SHARED_PATH)
		}
		interval = 5000
		trackerObj = new ServiceTracker(context, JobFacade.class.getName(), this)
		trackerObj.open()
	}

	public boolean filterFile(File file) {
		return file.getName().endsWith(".job") || file.getName().endsWith(".cls") || file.getName().endsWith(".trg") || file.getName().endsWith(".instances");
	}

	public boolean filterFile(SmbFile file) {
		return file.getName().endsWith(".job") || file.getName().endsWith(".cls") || file.getName().endsWith(".trg") || file.getName().endsWith(".instances");
	}

	public String getDirectory() {
		return dir;
	}

	public long getInterval() {
		return interval;
	}

	public String getUsername() {
		return this.shared_user;
	}
	
	public String getPassword() {
		return this.shared_pass;
	}

	public String getHost() {
		return this.shared_host;
	}

	public boolean notifyAdded(File target) throws IOException {
		if (jobfacade == null) {
			// falseを返せばこのファイルは未処理としてマークされる。
			return false
		}
		
		return processFile(target);
	}

	public boolean notifyAdded(SmbFile target) throws IOException {
		if (jobfacade == null) {
			return false
		}
		println "File added : " + target.getName()
		return processFile(target);
	}

	public boolean notifyDeleted(File target) throws IOException {
		return false;
	}

	public boolean notifyDeleted(SmbFile target) throws IOException {
		return false;
	}

	public boolean notifyModified(File target) throws IOException {
		if (jobfacade == null) {
			// falseを返せばこのファイルは未処理としてマークされる。
			return false
		}
		
		return processFile(target);
	}

	public boolean notifyModified(SmbFile target) throws IOException {
		if (jobfacade == null) {
			// falseを返せばこのファイルは未処理としてマークされる。
			return false
		}
		println "File Modified : " + target.getName()
		return processFile(target);
	}

	private boolean processFile(File target) {
		if (jobdsl != null) {
			if (target.getName().endsWith(".job")) {
				return jobdsl.processJob(target)
			} else if (target.getName().endsWith(".cls")) {
				return jobdsl.processCls(target)
			} else if (target.getName().endsWith(".trg")) {
				return jobdsl.processTrigger(target)
			} else if (target.getName().endsWith(".instances")) {
				return jobdsl.processInstances(target)
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean processFile(SmbFile target) {
		if (jobdsl != null) {
			if (target.getName().endsWith(".job")) {
				return jobdsl.processJob(target)
			} else if (target.getName().endsWith(".cls")) {
				return jobdsl.processCls(target)
			} else if (target.getName().endsWith(".trg")) {
				return jobdsl.processTrigger(target)
			} else if (target.getName().endsWith(".instances")) {
				return jobdsl.processInstances(target)
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * ServiceTrackerCustomizer.addingService
	 * 以下は、ServiceTrackerCustomizerの実装部
	 *  (http://www.osgi.org/javadoc/r4v42/org/osgi/util/tracker/ServiceTrackerCustomizer.html)
	 */
	public Object addingService(ServiceReference reference) {
		def oservice = context.getService(reference);
		if (oservice instanceof JobFacade) {
			jobfacade = oservice
			// prepare jobdsl object here.
			def JobDsl = shell.getClassLoader().loadClass("JobDsl")
			jobdsl = JobDsl.newInstance(shell, jobfacade, context)
		}
		return oservice
	}

	/**
	 * ServiceTrackerCustormizer.modifiedService
	 */
	public void modifiedService(ServiceReference reference, Object service) {
	}

	/**
	 * ServiceTrackerCustomizer.removedService
	 */
	public void removedService(ServiceReference reference, Object service)  {
		if (service == jobfacade) {
			jobfacade = null
			jobdsl = null
		}
	}
}

