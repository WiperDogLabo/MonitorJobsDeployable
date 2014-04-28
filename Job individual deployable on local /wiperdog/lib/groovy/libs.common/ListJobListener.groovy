
import org.wiperdog.directorywatcher.Listener
import org.apache.log4j.Logger
import org.osgi.framework.BundleContext
import org.osgi.framework.FrameworkUtil
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceReference
import org.osgi.util.tracker.ServiceTracker
import org.osgi.util.tracker.ServiceTrackerCustomizer


/**
 * Listerner using for watching change from file : etc/listJobs.cfg
 */
 */
class ListJobListener implements Listener,ServiceTrackerCustomizer {
	def dir
	def interval
	def context
	def trackerObj
	def properties
	static listJobs
	static listJobsPrevious
	def jobdsl
	public ListJobListener(BundleContext ctx) {
		this.context = ctx
		MonitorJobConfigLoader configLoader = new MonitorJobConfigLoader(context)
		properties = configLoader.getProperties();
		dir = properties.get(ResourceConstants.SYSTEM_PROPERTIES_FILE_DIRECTORY)
		interval = 5000
		trackerObj = new ServiceTracker(context, JobDSLService.class.getName(), this)
		trackerObj.open()
	}

	public boolean filterFile(File file) {
		return file.getName().equals("listJobs.cfg");
	}

	public String getDirectory() {
		return dir;
	}

	public long getInterval() {
		return interval;
	}
	public List<String> getListJobs() {
		return listJobs;
	}

	public boolean notifyAdded(File target) throws IOException {
		def content = target.getText();
		listJobsPrevious = null
		listJobs = new ArrayList<String>();
		content.eachLine { line->
			if(!line.startsWith("#") && line.trim() != ""){
				listJobs.add(line.trim());
			}
		}
		jobdsl.setListJobs(listJobs)
		listJobsPrevious = listJobs
		return true
	}

	public boolean notifyDeleted(File target) throws IOException {
		listJobs.each {
			jobdsl.removeScheduledJob(it.substring(0, it.indexOf(".job")))
		}
		return false;
	}

	public boolean notifyModified(File target) throws IOException {
		try{
			//Read listJobs.cfg			
			def content = target.getText();
			listJobs = new ArrayList<String>();
			content.eachLine { line->
				if(!line.startsWith("#") && line.trim() != ""){
					listJobs.add(line.trim());
				}
			}
			//jobdsl using listJobs to decided which job is run or not
			jobdsl.setListJobs(listJobs)

			def jobDirectory = properties.get(ResourceConstants.JOB_DIRECTORY)
			def instDirectory = properties.get(ResourceConstants.JOBINST_DIRECTORY)
			
			// If new job added ,process it
			listJobs.each {jobCurr ->
				if (!listJobsPrevious.contains(jobCurr)) {
					File jobFileAdd = new File(jobDirectory + "/" + jobCurr)
					//File instFileAdd = new File(jobDirectory + "/" + jobCurr.replace(".job", ".instances"))
					File instFileAdd =  new File(instDirectory +"/" + jobCurr.replace(".job", ".instances"))
					println instFileAdd
					jobdsl.processJob(jobFileAdd)
					if (instFileAdd.exists()) {
						jobdsl.processInstances(instFileAdd)
					}
				}
			}
			// if job not avaible in listJobs.cfg , remove it from scheduler
			listJobsPrevious.each {jobPrev ->
				if (!listJobs.contains(jobPrev)) {
					jobdsl.removeScheduledJob(jobPrev.substring(0, jobPrev.indexOf(".job")))
				}
			}
			listJobsPrevious = listJobs
		}catch(Exception ex) {
			println ex
		}
		return true
	}

	public Object addingService(ServiceReference reference) {
		jobdsl = context.getService(reference);
		return jobdsl
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
	}

}

