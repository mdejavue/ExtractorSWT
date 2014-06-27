package de.s9mtmeis.thesis.swt;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesConfig;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.amazonaws.services.elasticmapreduce.util.StepFactory;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

public class Start {

	protected Shell shlCommoncrawlUi;
	private Text txtAccessToken;
	private Text txtSecret;
	private Text txtBucketName;
	private Text txtClusterName;
	private Text txtLogUri;
	private Text txtJarUri;
	private Text txtMainClass;
	private Text txtOutputUri;
	private Text txtJobflowId;
	private Display display;
	private Text txtSpecificInput;
	private String valInputFile;
	private String valOutputFile;
	private String matchersString;
	private String extractorsString;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		Start window = null;
		try {
			window = new Start();
			window.open();
		} catch (Exception e) {
			MessageBox messageBox = new MessageBox(window.shlCommoncrawlUi, SWT.ICON_ERROR | SWT.ABORT);
			messageBox.setMessage(e.getMessage());
			messageBox.open();
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shlCommoncrawlUi.open();
		shlCommoncrawlUi.layout();
		shlCommoncrawlUi.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) 	 {	    	
		    	saveSettings();
		        event.doit = true;
		      }
		    });
		while (!shlCommoncrawlUi.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlCommoncrawlUi = new Shell();
		shlCommoncrawlUi.setSize(800, 600);
		shlCommoncrawlUi.setText("SimpleCrawl UI - prototype");
		RowLayout rl_shlCommoncrawlUi = new RowLayout(SWT.HORIZONTAL);
		rl_shlCommoncrawlUi.spacing = 0;
		rl_shlCommoncrawlUi.marginTop = 0;
		rl_shlCommoncrawlUi.marginRight = 0;
		rl_shlCommoncrawlUi.marginLeft = 0;
		rl_shlCommoncrawlUi.marginBottom = 0;
		shlCommoncrawlUi.setLayout(rl_shlCommoncrawlUi);
		


		
		Group grpCredentials = new Group(shlCommoncrawlUi, SWT.NONE);
		grpCredentials.setLayoutData(new RowData(270, 55));
		grpCredentials.setText("AWS Credentials");
		
		
		txtAccessToken = new Text(grpCredentials, SWT.BORDER);
		txtAccessToken.setText("Access token");
		txtAccessToken.setBounds(10, 10, 265, 19);
		
		txtSecret = new Text(grpCredentials, SWT.BORDER);
		txtSecret.setText("Secret");
		txtSecret.setBounds(10, 35, 265, 19);
		
		Group grpEnableModules = new Group(shlCommoncrawlUi, SWT.NONE);
		grpEnableModules.setLayoutData(new RowData(500, 55));
		grpEnableModules.setText("Enable Modules");
		
		Group grpS = new Group(shlCommoncrawlUi, SWT.NONE);
		grpS.setLayoutData(new RowData(782, SWT.DEFAULT));
		grpS.setText("S3 Simple Storage Service");
		
		final Group grpSetupCluster = new Group(shlCommoncrawlUi, SWT.NONE);
		grpSetupCluster.setEnabled(false);
		grpSetupCluster.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		grpSetupCluster.setLayoutData(new RowData(252, 380));
		grpSetupCluster.setText("1 Setup Cluster");
		
		
		final Group grpConfigureStep = new Group(shlCommoncrawlUi, SWT.NONE);
		grpConfigureStep.setEnabled(false);
		grpConfigureStep.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		grpConfigureStep.setLayoutData(new RowData(252, 380));
		grpConfigureStep.setText("2 Configure Step");
		
		
		final Group grpValidateOutput = new Group(shlCommoncrawlUi, SWT.NONE);
		grpValidateOutput.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		grpValidateOutput.setEnabled(false);
		grpValidateOutput.setLayoutData(new RowData(251, 380));
		grpValidateOutput.setText("3 Validate Output");
		
		
		Group group = new Group(shlCommoncrawlUi, SWT.NONE);
		group.setLayoutData(new RowData(784, 16));
		
		final Label lblMessageText = new Label(group, SWT.NONE);
		lblMessageText.setBounds(75, 5, 540, 14);
		lblMessageText.setText("...");
		
		Label lblFile = new Label(grpValidateOutput, SWT.NONE);
		lblFile.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		lblFile.setBounds(20, 45, 45, 14);
		lblFile.setText("Input:");
		
		Label lblSize = new Label(grpValidateOutput, SWT.NONE);
		lblSize.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		lblSize.setBounds(20, 64, 45, 14);
		lblSize.setText("Size:");
		
		final Label lblNone = new Label(grpValidateOutput, SWT.NONE);
		lblNone.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		lblNone.setBounds(81, 45, 170, 14);
		lblNone.setText("None");
		
		final Label lblNa = new Label(grpValidateOutput, SWT.NONE);
		lblNa.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		lblNa.setBounds(81, 64, 180, 14);
		lblNa.setText("n/a");
		
		Label lblNewLabel = new Label(grpValidateOutput, SWT.NONE);
		lblNewLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		lblNewLabel.setBounds(20, 127, 45, 14);
		lblNewLabel.setText("Output:");
		
		final Label lblNewLabel_1 = new Label(grpValidateOutput, SWT.NONE);
		lblNewLabel_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		lblNewLabel_1.setBounds(81, 127, 170, 14);
		lblNewLabel_1.setText("None");
		
		Button btnChooseOutputFile = new Button(grpValidateOutput, SWT.NONE);
		btnChooseOutputFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(shlCommoncrawlUi, SWT.OPEN);
		        fd.setText("Open");
		        fd.setFilterPath(null);
		        String[] filterExt = { "*.*" };
		        fd.setFilterExtensions(filterExt);
		        valInputFile = fd.open();
		        
		        File inputFile = new File(valInputFile);
		        lblNone.setText(inputFile.getName());
		        DecimalFormat df = new DecimalFormat("#.##");
		        lblNa.setText(df.format(inputFile.length() / (1024.0 * 1024.0)) + " MByte");
		        
			}
		});
		btnChooseOutputFile.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnChooseOutputFile.setBounds(10, 10, 231, 28);
		btnChooseOutputFile.setText("Choose File from Disk");
		
		Button btnValidateWithRapper = new Button(grpValidateOutput, SWT.NONE);
		btnValidateWithRapper.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String[] cmd = {
							"/bin/sh",
							"-c",
							"/usr/local/bin/rapper -i ntriples -r " + valInputFile + " > " + valOutputFile,
							};					
					Runtime.getRuntime().exec(cmd);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnValidateWithRapper.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnValidateWithRapper.setBounds(10, 275, 231, 28);
		btnValidateWithRapper.setText("Start");
		
		Button btnRemoveTriples = new Button(grpValidateOutput, SWT.NONE);
		btnRemoveTriples.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
			            "", "Seperate Patterns with Semicolon (;)", "!.css; /product; /offer; #offer; /review", null);
			        if (dlg.open() == Window.OK) {
			        	
			        	String[] values = dlg.getValue().split(";");
			        	String negatives = "(";
			        	String positives = "(";
			        	
			        	for ( String s : values )
			        	{
			        		s = s.trim();
			        		if (s.startsWith("!"))
			        		{
			        			negatives += s.substring(1) + "|" ;
			        		}
			        		else
			        		{
			        			positives += s + "|";
			        		}
			        	}
			        	
			        	negatives = negatives.substring(0,negatives.length()-1) + ")";
			        	positives = positives.substring(0,positives.length()-1) + ")";
			        	
				        String[] cmd1 = {
									"/bin/sh",
									"-c",
									"grep -v " + negatives + " > " + valOutputFile + "_cleaned",
									};	
				        
				        String[] cmd2 = {
									"/bin/sh",
									"-c",
									"grep " + positives + " > " + valOutputFile + "_cleaned" ,
									};	
				        
							try {
								if (negatives.length() > 1) {
									Runtime.getRuntime().exec(cmd1).waitFor();
								}
								
								if (positives.length() > 1) {
									Runtime.getRuntime().exec(cmd2);
								}
							
							} catch (Exception e1) {
								e1.printStackTrace();
							}
			        }
			}
		});
		btnRemoveTriples.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnRemoveTriples.setBounds(158, 207, 34, 28);
		btnRemoveTriples.setText("...");
		
		
		
		Button btnNewButton = new Button(grpValidateOutput, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(shlCommoncrawlUi, SWT.SAVE);
		        fd.setText("Save");
		        fd.setFilterPath(null);
		        String[] filterExt = { "*.nt" };
		        fd.setFilterExtensions(filterExt);
		        
		        valOutputFile = fd.open();
		        File outputFile = new File(valOutputFile);
		        lblNewLabel_1.setText(outputFile.getName());
			}
		});
		btnNewButton.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnNewButton.setBounds(10, 93, 231, 28);
		btnNewButton.setText("Choose File Destination");
		
		Button btnAddTimestamps = new Button(grpValidateOutput, SWT.CHECK);
		btnAddTimestamps.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnAddTimestamps.setBounds(20, 166, 172, 18);
		btnAddTimestamps.setText("Add Timestamps");
		
		Button btnAddUrls = new Button(grpValidateOutput, SWT.CHECK);
		btnAddUrls.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnAddUrls.setText("Add URLs");
		btnAddUrls.setBounds(20, 190, 172, 18);
		
		Button btnCleanupTriples = new Button(grpValidateOutput, SWT.CHECK);
		btnCleanupTriples.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnCleanupTriples.setText("Clean-up Triples");
		btnCleanupTriples.setBounds(20, 211, 132, 18);
		
		Button btnValidate = new Button(grpValidateOutput, SWT.CHECK);
		btnValidate.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnValidate.setText("Validate");
		btnValidate.setBounds(20, 235, 172, 18);
		
		ProgressBar progressBar = new ProgressBar(grpValidateOutput, SWT.NONE);
		progressBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		progressBar.setBounds(20, 309, 208, 28);
		
		Button btnUpload = new Button(grpValidateOutput, SWT.NONE);
		btnUpload.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnUpload.setBounds(10, 343, 231, 28);
		btnUpload.setText("Upload");
		
		
		
		final Button btnSetupCluster = new Button(grpEnableModules, SWT.CHECK);
		btnSetupCluster.setBounds(30, 27, 128, 18);
		btnSetupCluster.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnSetupCluster.getSelection()) {
					grpSetupCluster.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
					grpSetupCluster.setEnabled(true);
					for ( Control c : grpSetupCluster.getChildren() )						
						c.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));	
				}
				else
				{
					grpSetupCluster.setForeground(getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
					grpSetupCluster.setEnabled(false);
					for ( Control c : grpSetupCluster.getChildren() )						
						c.setForeground(getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
				}
			}
		});
		btnSetupCluster.setText("1 Setup Cluster");
		
		final Button btnConfigureStep = new Button(grpEnableModules, SWT.CHECK);
		btnConfigureStep.setBounds(164, 27, 150, 18);
		btnConfigureStep.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnConfigureStep.getSelection()) {
					grpConfigureStep.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
					grpConfigureStep.setEnabled(true);
					for ( Control c : grpConfigureStep.getChildren() )						
						c.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));	
				}
				else
				{
					grpConfigureStep.setForeground(getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
					grpConfigureStep.setEnabled(false);
					for ( Control c : grpConfigureStep.getChildren() )						
						c.setForeground(getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
				}
			}
		});
		btnConfigureStep.setText("2 Configure Step");
		
		final Button btnValidateOutput = new Button(grpEnableModules, SWT.CHECK);
		btnValidateOutput.setBounds(311, 27, 144, 18);
		btnValidateOutput.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnValidateOutput.getSelection()) {
					grpValidateOutput.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
					grpValidateOutput.setEnabled(true);
					for ( Control c : grpValidateOutput.getChildren() )						
						c.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));	
				}
				else
				{
					grpValidateOutput.setForeground(getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
					grpValidateOutput.setEnabled(false);
					for ( Control c : grpValidateOutput.getChildren() )						
						c.setForeground(getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
				}
			}
		});
		btnValidateOutput.setText("3 Validate Output");
		
		
		Button btnGotoSBrowser = new Button(grpS, SWT.NONE);
		btnGotoSBrowser.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openWebpage(new URI("https://console.aws.amazon.com/s3/home?region=us-east-1"));
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnGotoSBrowser.setBounds(617, 6, 165, 28);
		btnGotoSBrowser.setText("Open S3 Browser");
		
		txtBucketName = new Text(grpS, SWT.BORDER);
		txtBucketName.setText("mybucket");
		txtBucketName.setBounds(10, 10, 170, 19);
		
		Button btnApplyToEmr = new Button(grpS, SWT.NONE);
		btnApplyToEmr.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					AmazonS3Client s3 = new AmazonS3Client(
						    new BasicAWSCredentials(txtAccessToken.getText(), txtSecret.getText()));
					Bucket bucket = s3.createBucket(txtBucketName.getText()); //throws exception
					MessageBox messageBox = new MessageBox(shlCommoncrawlUi, SWT.ICON_WORKING | SWT.OK);
					messageBox.setMessage("Bucket is ready");
					messageBox.open();
					lblMessageText.setText("Bucket '" + bucket.getName() + "' is ready." );
					txtLogUri.setText("s3n://" + bucket.getName() + "/emr/logs");
					txtJarUri.setText("s3n://" + bucket.getName() + "/upload/job.jar");
					txtOutputUri.setText("s3n://" + bucket.getName() + "/emr/output");
				}
				catch (Exception e3) {
					MessageBox messageBox = new MessageBox(shlCommoncrawlUi, SWT.ICON_ERROR | SWT.ABORT);
					messageBox.setMessage(e3.getMessage());
					messageBox.open();
				}
			}
		});
		btnApplyToEmr.setBounds(186, 6, 108, 28);
		btnApplyToEmr.setText("Apply");

		Button btnCreateCluster = new Button(grpSetupCluster, SWT.NONE);
		
		btnCreateCluster.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnCreateCluster.setText("Create Cluster/Jobflow");
		btnCreateCluster.setBounds(10, 282, 238, 28);
		
		Button btnGotoClusterList = new Button(grpSetupCluster, SWT.NONE);
		btnGotoClusterList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openWebpage(new URI("https://console.aws.amazon.com/elasticmapreduce/vnext/home?region=us-east-1#"));
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnGotoClusterList.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnGotoClusterList.setText("Goto ClusterList");
		btnGotoClusterList.setBounds(10, 3, 244, 28);
		
		txtClusterName = new Text(grpSetupCluster, SWT.BORDER);
		txtClusterName.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		txtClusterName.setToolTipText("");
		txtClusterName.setText("MySimpleCrawl");
		txtClusterName.setBounds(10, 37, 244, 19);
		
		final Combo cmbMasterType = new Combo(grpSetupCluster, SWT.READ_ONLY);
		cmbMasterType.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		cmbMasterType.setItems(new String[] {"Master Type", "m1.small", "m1.medium", "m1.large", "m1.xlarge"});
		cmbMasterType.setBounds(10, 62, 119, 22);
		cmbMasterType.select(0);
		
		final Combo cmbSlaveType = new Combo(grpSetupCluster, SWT.READ_ONLY);
		cmbSlaveType.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		cmbSlaveType.setItems(new String[] {"Slave Type", "m1.small", "m1.medium", "m1.large", "m1.xlarge"});
		cmbSlaveType.setBounds(135, 62, 119, 22);
		cmbSlaveType.select(0);
		
		final Spinner spnNumInstances = new Spinner(grpSetupCluster, SWT.BORDER);
		spnNumInstances.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		spnNumInstances.setBounds(167, 89, 87, 22);
		
		Label label = new Label(grpSetupCluster, SWT.NONE);
		label.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		label.setText("number of instances:");
		label.setBounds(10, 90, 132, 19);
		
		txtLogUri = new Text(grpSetupCluster, SWT.BORDER);
		txtLogUri.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		txtLogUri.setText("s3n://mybucket/emr/logs");
		txtLogUri.setBounds(10, 115, 244, 19);
		
		Label label_1 = new Label(grpSetupCluster, SWT.NONE);
		label_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		label_1.setText("Hadoop Version: 1.0.3");
		label_1.setBounds(10, 140, 132, 14);
		
		Button button_3 = new Button(grpSetupCluster, SWT.NONE);
		button_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		button_3.setText("Calculate EC2 Costs");
		button_3.setBounds(10, 171, 151, 28);
		
		Button button_4 = new Button(grpSetupCluster, SWT.NONE);
		button_4.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		button_4.setText("Terminate Cluster/Jobflow");
		button_4.setBounds(10, 348, 238, 28);
		
		CLabel lblStatus1 = new CLabel(group, SWT.NONE);
		lblStatus1.setBounds(648, 2, -622, 297);
		lblStatus1.setText("1");
		lblStatus1.setImage(new Image(getDisplay(), "img/circle-outline-16.ico"));
		lblStatus1.pack();
		
		CLabel lblStatus2 = new CLabel(group, SWT.NONE);
		lblStatus2.setBounds(701, 2, 13, 24);
		lblStatus2.setText("2");
		lblStatus2.setImage(new Image(getDisplay(), "img/circle-outline-16.ico"));
		lblStatus2.pack();
		
		CLabel lblStatus3 = new CLabel(group, SWT.NONE);
		lblStatus3.setBounds(749, 2, -574, 297);
		lblStatus3.setText("3");
		lblStatus3.setImage(new Image(getDisplay(), "img/circle-outline-16.ico"));
		lblStatus3.pack();
		
		Button button_5 = new Button(grpConfigureStep, SWT.NONE);
		button_5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				try {
				AWSCredentials credentials = new BasicAWSCredentials(txtAccessToken.getText(), txtSecret.getText());
				AmazonElasticMapReduceClient emr = new AmazonElasticMapReduceClient(credentials);
				 
				   StepConfig customStep = new StepConfig()
				       .withName("Custom Crawl")
				       .withActionOnFailure("TERMINATE_JOB_FLOW")
				       .withHadoopJarStep(new HadoopJarStepConfig()
				       		.withJar(txtJarUri.getText())
				   			.withMainClass(txtMainClass.getText())
				   			.withArgs("inputPath=" + txtSpecificInput.getText(),
				   					  "outputPath=" + txtOutputUri.getText(),
				   					  "matchers=" + matchersString,
				   					  "extractors=" + extractorsString));
				
				   AddJobFlowStepsRequest request = new AddJobFlowStepsRequest()
				   											.withJobFlowId(txtJobflowId.getText())
				   											.withSteps(customStep);
				   
				   System.out.println(request.toString());
				   
				   emr.addJobFlowSteps(request);
				   }
				catch (Exception e2)
				{
					MessageBox messageBox = new MessageBox(shlCommoncrawlUi, SWT.ICON_ERROR | SWT.ABORT);
					messageBox.setMessage(e2.getMessage());
					messageBox.open();
				}
			}
		});
		button_5.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		button_5.setText("Add Step");
		button_5.setBounds(10, 348, 223, 28);
		
		txtJarUri = new Text(grpConfigureStep, SWT.BORDER);
		txtJarUri.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		txtJarUri.setText("s3n://mybucket/upload/job.jar");
		txtJarUri.setBounds(10, 20, 230, 19);
		
		txtMainClass = new Text(grpConfigureStep, SWT.BORDER);
		txtMainClass.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		txtMainClass.setText("de.myjob.Main");
		txtMainClass.setBounds(10, 45, 230, 19);
		
		txtOutputUri = new Text(grpConfigureStep, SWT.BORDER);
		txtOutputUri.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		txtOutputUri.setText("s3n://mybucket/emr/output");
		txtOutputUri.setBounds(10, 70, 230, 19);
		
		Button button_6 = new Button(grpConfigureStep, SWT.NONE);
		button_6.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
			            "", "Seperate Matchers with Semicolon (;)", "(property|typeof|about|resource)\\s*=; (itemscope|itemprop\\s*=); hproduct", null);
			        if (dlg.open() == Window.OK) {
			        	
			        	String tmpString = "";

			        	String[] values = dlg.getValue().split(";");
			        	
			        	for ( String s : values ) {
			        		s = s.trim();
			        		tmpString = tmpString + " ;; " + s ;
			        	}
			        	
			        	matchersString = tmpString.substring(4);
			        }
			}
		});
		button_6.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		button_6.setText("Set Matchers");
		button_6.setBounds(10, 174, 139, 28);
		
		final Button button_9 = new Button(grpConfigureStep, SWT.RADIO);
		button_9.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (button_9.getSelection()) {
					txtSpecificInput.setVisible(true);
				}
				else
				{
					txtSpecificInput.setVisible(false);
				}
			}
		});
		button_9.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		button_9.setText("Specific Crawl");
		button_9.setBounds(10, 234, 117, 18);
		
		Button button_10 = new Button(grpConfigureStep, SWT.RADIO);
		button_10.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		button_10.setText("Random");
		button_10.setBounds(133, 234, 88, 18);
		
		txtJobflowId = new Text(grpConfigureStep, SWT.BORDER);
		txtJobflowId.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		txtJobflowId.setText("Jobflow Id");
		txtJobflowId.setBounds(10, 323, 230, 19);
		
		Button btnSelectExtractors = new Button(grpConfigureStep, SWT.NONE);
		btnSelectExtractors.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
			            "", "Seperate Extractors with Semicolon (;)", "html-rdfa11; html-microdata; html-mf-hproduct" , null);
			        if (dlg.open() == Window.OK) {

			        	String tmpString = "";

			        	String[] values = dlg.getValue().split(";");
			        	
			        	for ( String s : values ) {
			        		s = s.trim();
			        		tmpString = tmpString + " ;; " + s ;
			        	}
			        	
			        	extractorsString = tmpString.substring(4);
			        }
			}
		});
		btnSelectExtractors.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		btnSelectExtractors.setBounds(10, 140, 139, 28);
		btnSelectExtractors.setText("Set Extractors");
		
		txtSpecificInput = new Text(grpConfigureStep, SWT.BORDER);
		txtSpecificInput.setText("s3n://input/path");
		txtSpecificInput.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		txtSpecificInput.setBounds(10, 262, 230, 19);
		txtSpecificInput.setVisible(false);
		
		btnCreateCluster.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				try {
				AWSCredentials credentials = new BasicAWSCredentials(txtAccessToken.getText(), txtSecret.getText());
				AmazonElasticMapReduceClient emr = new AmazonElasticMapReduceClient(credentials);

				   StepFactory stepFactory = new StepFactory();

				   StepConfig enabledebugging = new StepConfig()
				       .withName("Enable debugging")
				       .withActionOnFailure("TERMINATE_JOB_FLOW")
				       .withHadoopJarStep(stepFactory.newEnableDebuggingStep());

				   RunJobFlowRequest request = new RunJobFlowRequest()
				       .withName(txtClusterName.getText())
				       .withSteps(enabledebugging)
				       .withLogUri(txtLogUri.getText())
				       .withInstances(new JobFlowInstancesConfig()
				           .withHadoopVersion("1.0.3")
				           .withInstanceCount(spnNumInstances.getSelection())
				           .withKeepJobFlowAliveWhenNoSteps(true)
				           .withMasterInstanceType(cmbMasterType.getText())
				           .withSlaveInstanceType(cmbSlaveType.getText()));

				   RunJobFlowResult result = emr.runJobFlow(request);	
				}
				catch (Exception e2)
				{
					MessageBox messageBox = new MessageBox(shlCommoncrawlUi, SWT.ICON_ERROR | SWT.ABORT);
					messageBox.setMessage(e2.getMessage());
					messageBox.open();
				}
			}
		});
		
		
		Label lblMessages = new Label(group, SWT.NONE);
		lblMessages.setBounds(10, 5, 59, 14);
		lblMessages.setText("Messages:");
		
		
		loadSettings();
		
	}

	protected Display getDisplay() {
		return display;
	}
	
	private void saveSettings() {
		// saving your data
    	PrintWriter writer;
		try {
			writer = new PrintWriter("moderator.settings", "UTF-8");
	    	writer.println("accessToken=" + txtAccessToken.getText());
	    	writer.println("accessSecret=" + txtSecret.getText());
	    	writer.println("bucketName=" + txtBucketName.getText());
	    	writer.println("clusterName=" + txtClusterName.getText());
	    	writer.println("masterType=" + "na");
	    	writer.println("slaveType=" + "na");
	    	writer.println("numInstances=" + "na");
	    	writer.println("logUri=" + txtLogUri.getText());
	    	writer.println("jarUri=" + txtJarUri.getText());
	    	writer.println("mainClass=" + txtMainClass.getText());
	    	writer.println("outputUri=" + txtOutputUri.getText());
	    	writer.println("extractors=" + "na");
	    	writer.println("matchers=" + "na");
	    	writer.println("inputUri=" + txtSpecificInput.getText());
	    	writer.println("jobflowId=" + txtJobflowId.getText());
	    	writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
	}
	
	private void loadSettings() {
		
		HashMap<String, Control> controls = new HashMap<String, Control>();
		controls.put("accessToken", txtAccessToken);
		controls.put("accessSecret", txtSecret);
		controls.put("bucketName", txtBucketName);
		controls.put("clusterName", txtClusterName);
		controls.put("masterType", null);
		controls.put("slaveType", null);	
		controls.put("numInstances", null);
		controls.put("logUri", txtLogUri);
		controls.put("jarUri", txtJarUri);
		controls.put("mainClass", txtMainClass);
		controls.put("outputUri", txtOutputUri);
		controls.put("extractors", null);
		controls.put("matchers", null);
		controls.put("inputUri", txtSpecificInput);
		controls.put("jobflowId", txtJobflowId);
				
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("moderator.settings"));
			String line;
			while ((line = br.readLine()) != null) {
				  String[] split = line.split("=");
				  Control c = controls.get(split[0]);
				  if (c != null) {
					if ( c instanceof Text ) {
						((Text)c).setText(split[1]);
					}
				  }
				}
				br.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public static void openWebpage(URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}
}

