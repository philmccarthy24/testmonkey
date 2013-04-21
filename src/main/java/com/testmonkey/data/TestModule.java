package com.testmonkey.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.testmonkey.util.PathsHelper;

@XmlRootElement(name="testmodule")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestModule {
	@XmlElement(name="name")
	private String moduleName;
	
	@XmlElement(name="description")
	private String moduleDescription;
	
	@XmlElement(name="path")
	private String moduleFilePath;
	
	public TestModule() {}
	
	/**
	 * @return the moduleName
	 */
	public String getModuleName() {
		return moduleName;
	}
	/**
	 * @param moduleName the moduleName to set
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	/**
	 * @param fromPath the path to generate the module name from
	 */
	public void setModuleDefaultName(String fromPath) {
		this.moduleName = PathsHelper.getFileNameNoExtensionFromPath(fromPath);
	}
	/**
	 * @return the moduleDescription
	 */
	public String getModuleDescription() {
		return moduleDescription;
	}
	/**
	 * @param moduleDescription the moduleDescription to set
	 */
	public void setModuleDescription(String moduleDescription) {
		this.moduleDescription = moduleDescription;
	}
	/**
	 * @param moduleName the module name to generate the description from
	 */
	public void setModuleDefaultDescription(String moduleName) {
		this.moduleDescription = "Tests in the " + moduleName + " google test harness";
	}
	/**
	 * @return the moduleFilePath
	 */
	public String getModuleFilePath() {
		return moduleFilePath;
	}
	/**
	 * @param moduleFilePath the moduleFilePath to set
	 */
	public void setModuleFilePath(String moduleFilePath) {
		this.moduleFilePath = moduleFilePath;
	}
}
