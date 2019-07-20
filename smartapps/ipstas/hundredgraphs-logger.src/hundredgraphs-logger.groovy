/*
 *  HundredGraphs Logger 
 *
 *  Author: 
 *    HundredGraphs
 *
 *  Based on https://github.com/krlaframboise/SmartThings/
 *    
 *  Changelog:
 * 		Adapted to use HundredGraphs cloud data aggregation/visualization SaaS
 *
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of
 *  the License at:
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the License is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 *  OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
 
 /*
 * 	Original license:
 *
 *  Simple Event Logger - SmartApp v 1.4.1
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    https://github.com/krlaframboise/SmartThings/tree/master/smartapps/krlaframboise/simple-event-logger.src#simple-event-logger
 *
 *    1.0.0 (12/26/2016)
 *      - Initial Release
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of
 *  the License at:
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the License is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 *  OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
 
include 'asynchttp_v1'
 
definition(
    name: "HundredGraphs Logger",
    namespace: "ipstas",
    author: "HundredGraphs",
    description: "Allows you to choose devices and attributes and it logs the device, event name, event value, event time, and event description of all the events that have occured since the last time it ran.",
    category: "My Apps",
    iconUrl: "https://res.cloudinary.com/orangry/image/upload/c_scale,w_30/v1554557246/hundredgraphs/HundredGraphs_620x620.png",
    iconX2Url: "https://res.cloudinary.com/orangry/image/upload/c_scale,w_60/v1554557246/hundredgraphs/HundredGraphs_620x620.png",
    iconX3Url: "https://res.cloudinary.com/orangry/image/upload/c_scale,w_100/v1554557246/hundredgraphs/HundredGraphs_620x620.png")
		
preferences {
	page(name: "mainPage")
	page(name: "devicesPage")
	page(name: "attributesPage")
	page(name: "attributeExclusionsPage")
	page(name: "optionsPage")
	page(name: "aboutPage")
	//page(name: "createTokenPage")
}

def version() { return "00.00.06" }
def gsVersion() { return "00.00.01" }

def mainPage() {
	dynamicPage(name:"mainPage", uninstall:true, install:true) {
		if (state.allConfigured && state.loggingStatus) {
			getLoggingStatusContent()
		}
		if (state.devicesConfigured) {
			section("Selected Devices") {
				getPageLink("devicesPageLink", "Tap to change", "devicesPage", null, buildSummary(getSelectedDeviceNames()))
			}
		}
		else {			
			getDevicesPageContent()
		}
		
		if (state.attributesConfigured) {
			section("Selected Events") {
				getPageLink("attributesPageLink", "Tap to change", "attributesPage", null, buildSummary(settings?.allowedAttributes?.sort()))
			}
			section ("Event Device Exclusions") {
				getPageLink("attributeExclusionsPageLink", "Select devices to exclude for specific events.", "attributeExclusionsPage")
			}
		}
		else {
			getAttributesPageContent()
		}
				
		if (!state.optionsConfigured) {
			getOptionsPageContent()
		}
		
		section("  ") {
			if (state.optionsConfigured) {
				getPageLink("optionsPageLink", "Other Options", "optionsPage", null, "Tap to set")
			}
			// input "appName", "text",
				// title: "Assign an app name",
				// defaultValue: "HundredGraphs Logger",
				// required: false
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
			if (state.installed) {		
				getPageLink("aboutPageLink", "About HundredGraphs Logger", "aboutPage", null, "Tap to view documentation, version and additional information.", "https://res.cloudinary.com/orangry/image/upload/v1554557246/hundredgraphs/HundredGraphs_620x620.png")
			}
		}
		section("  ") {
			paragraph "  ", required: false
		}
	}
}

private getLoggingStatusContent() {
	if (state.loggingStatus?.success != null) {
		section("Logging Status") {			
			def status = getFormattedLoggingStatus()
			
			paragraph required: false,
				"Version: ${version()}"			
			paragraph required: false,
				"Total Events Logged: ${status.totalEventsLogged}\nLast Execution:\n - Result: ${status.result}\n - Events From: ${status.start}\n - Events To: ${status.end}\n - Logged: ${status.eventsLogged}\n - Run Time: ${status.runTime}"
		}
	}
}

def aboutPage() {
	dynamicPage(name:"aboutPage") {
		section() {		
			def gsVerActual = state.loggingStatus?.gsVersion ?: "?"
			
			// def gsVerExpectedMsg = (gsVersion() == gsVerActual) ? "" : " (expected version is ${gsVersion()})"
		
			paragraph image: "https://res.cloudinary.com/orangry/image/upload/v1554557246/hundredgraphs/HundredGraphs_620x620.png",
				title: "HundredGraphs Logger\nby ipstas (@ipstas)",
				required: false,
				"Allows you to choose devices and attributes and it logs the device, event name, event value, event time, and event description of all the events that have occured since the last time it ran."
				
			paragraph title: "Version",
				required: false,
				"SmartApp: ${version()}"
				
			 href(name: "documentationLink",
				 title: "View Documentation",
				 required: false,
				 style: "external",
				 url: "http://htmlpreview.github.com/?https://github.com/ipstas/ipstas-smarththings-hundredgraphs/blob/master/README.md",
				 description: "Additional information about the SmartApp and installation instructions.")
		}		
	}
}

def devicesPage() {
	dynamicPage(name:"devicesPage") {
		getDevicesPageContent()
	}
}

private getDevicesPageContent() {
	section("Choose Devices") {
		paragraph "Selecting a device from one of the fields below lets the SmartApp know that the device should be included in the logging process."
		paragraph "Each device only needs to be selected once and which field you select it from has no effect on which events will be logged for it."
		paragraph "There's a field below for every capability, but you should be able to locate most of your devices in either the 'Actuators' or 'Sensors' fields at the top."		
		
		getCapabilities().each { 
			try {
				if (it.cap) {
					input "${it.cap}Pref", "capability.${it.cap}",
						title: "${it.title}:",
						multiple: true,
						hideWhenEmpty: true,
						required: false,
						submitOnChange: true
				}
			}
			catch (e) {
				logTrace "Failed to create input for ${it}: ${e.message}"
			}
		}
			
	}
}

def attributesPage() {
	dynamicPage(name:"attributesPage") {
		getAttributesPageContent()
	}
}

private getAttributesPageContent() {
	//def supportedAttr = getAllAttributes()?.sort()
	def supportedAttr = getSupportedAttributes()?.sort()
	if (supportedAttr) {
		section("Choose Events") {
			paragraph "Select all the events that should get logged for all devices that support them."
			paragraph "If the event you want to log isn't shown, verify that you've selected a device that supports it because only supported events are included."
			input "allowedAttributes", "enum",
				title: "Which events should be logged?",
				required: true,
				multiple: true,					
				submitOnChange: true,
				options: supportedAttr
		}
	}
	else {
		section("Choose Events") {
			paragraph "You need to select devices before you can choose events."
		}
	}
}

def attributeExclusionsPage() {
	dynamicPage(name:"attributeExclusionsPage") {		
		section ("Device Exclusions (Optional)") {
			
			def startTime = new Date().time
			
			if (settings?.allowedAttributes) {
				
				paragraph "If there are some events that should't be logged for specific devices, use the corresponding event fields below to exclude them."
				paragraph "You can also use the fields below to see which devices support each event."
				
				def devices = getSelectedDevices()?.sort { it.displayName }
				
				settings?.allowedAttributes?.sort()?.each { attr ->
				
					if (startTime && (new Date().time - startTime) > 15000) {
						paragraph "The SmartApp was able to load all the fields within the allowed time.  If the event you're looking for didn't get loaded, select less devices or attributes."
						startTime = null
					}
					else if (startTime) {				
						try {
							def attrDevices = (isAllDeviceAttr("$attr") ? devices : (devices?.findAll{ device ->
								device.hasAttribute("${attr}")
							}))?.collect { it.displayName }?.unique()
							if (attrDevices) {
								input "${attr}Exclusions", "enum",
									title: "Exclude ${attr} events:",
									required: false,
									multiple: true,
									options: attrDevices
							}
						}
						catch (e) {
							logWarn "${app.label} Error while getting device exclusion list for attribute ${attr}: ${e.message}"
						}
					}
				}
			}
		}
	}
}

def optionsPage() {
	dynamicPage(name:"optionsPage") {
		getOptionsPageContent()
	}
}

private getOptionsPageContent() {
	section ("Logging Options") {
		input "apiKey", "text",
			title: "API key:",
			defaultValue: "None",
			required: true
		input "node", "text",
			title: "Node (should be unique for each hub):",
			defaultValue: "1",
			required: true
		input "logFrequency", "enum",
			title: "Log Events Every:",
			required: false,
			defaultValue: "1 Minute",
			options: ["1 Minute", "5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "3 Hours"]
		input "logCatchUpFrequency", "enum",
			title: "Maximum Catch-Up Interval:\n(Must be greater than 'Log Events Every':",
			required: false,
			defaultValue: logCatchUpFrequencySetting,
			options: ["15 Minutes", "30 Minutes", "1 Hour", "2 Hours", "6 Hours"]
		input "maxEvents", "number",
			title: "Maximum number of events to log for each device per execution. (1 - 200)",
			range: "1..200",
			defaultValue: maxEventsSetting,
			required: false
		input "logDesc", "bool",
			title: "Log Event Descripion?",
			defaultValue: true,
			required: false
		input "useValueUnitDesc", "bool",
			title: "Use Value and Unit for Description?",
			defaultValue: true,
			required: false
		input "logReporting", "bool",
			title: "Include additional columns for short date and hour?",
			defaultValue: false,
			required: false
		input "deleteExtraColumns", "bool",
			title: "Delete Extra Columns?",
			description: "Enable this setting to increase the log size.",
			defaultValue: true,
			required: false
		input "archiveType", "enum",
			title: "Archive Type:",
			defaultValue: "None",
			submitOnChange: true,
			required: false,
			options: ["None", "Out of Space", "Events"]
		if (settings?.archiveType && !(settings?.archiveType in ["None", "Out of Space"])) {
			input "archiveInterval", "number",
				title: "Archive After How Many Events?",
				defaultValue: 50000,
				required: false,
				range: "100..100000"
		}
	}
	section("${getWebAppName()}") {		
		input "loggerAppUrl", "text",
			title: "${getWebAppName()} Url",
			defaultValue: "${loggerUrl()}",
			options: ["${loggerUrl()}", "${loggerUrlDev()}"]
			required: true
		paragraph "The url you enter into this field needs to start with: ${loggerUrl()}"	
	}
	
/* 	if (state.installed) {
		section("OAuth Token") {
			getPageLink("createTokenPageLink", "Generate New OAuth Token", "createTokenPage", null, state.endpoint ? "" : "The SmartApp was unable to generate an OAuth token which usually happens if you haven't gone into the IDE and enabled OAuth in this SmartApps settings.  Once OAuth is enabled, you can click this link to try again.")
		}
	} */
	
	section("Live Logging Options") {
		input "logging", "enum",
			title: "Types of messages to write to Live Logging:",
			multiple: true,
			required: false,
			defaultValue: ["debug", "info"],
			options: ["debug", "info", "trace"]
	}
}

def createTokenPage() {
	dynamicPage(name:"createTokenPage") {
		
		disposeAppEndpoint()
		initializeAppEndpoint()		
		
		section() {
			if (state.endpoint) {				
				paragraph "A new token has been generated."
			}
			else {
				paragraph "Unable to generate a new OAuth token.\n\n${getInitializeEndpointErrorMessage()}"				
			}
		}
	}
}

private getPageLink(linkName, linkText, pageName, args=null,desc="",image=null) {
	def map = [
		name: "$linkName", 
		title: "$linkText",
		description: "$desc",
		page: "$pageName",
		required: false
	]
	if (args) {
		map.params = args
	}
	if (image) {
		map.image = image
	}
	href(map)
}

private buildSummary(items) {
	def summary = ""
	items?.each {
		summary += summary ? "\n" : ""
		summary += "   ${it}"
	}
	return summary
}

def uninstalled() {
	logTrace "Executing uninstalled()"
	disposeAppEndpoint()
}

private disposeAppEndpoint() {
	if (state.endpoint) {
		try {
			logTrace "Revoking access token"
			revokeAccessToken()
		}
		catch (e) {
			logWarn "${app.label} Unable to remove access token: $e"
		}
		state.endpoint = ""
	}
}

def installed() {	
	logTrace "${app.label} Executing installed()"
	//initializeAppEndpoint()
	state.installed = true
}

def updated() {
	logTrace "${app.label} Executing updated()"
	state.installed = true
	
	unschedule()
	unsubscribe()
	
	//initializeAppEndpoint()
	
	logDebug "${app.label} [updated] freq: ${settings?.logFrequency}, url: ${settings?.loggerAppUrl}"
	
	state.app = "SmartThings"
	//state.version = ${version()}

	if (settings?.apiKey) {
		state.apiKey = settings?.apiKey
	}	else {
		logDebug "${app.label} Unconfigured - you need API key ${settings?.apiKey}"
	}

	if (settings?.apiKey && settings?.logFrequency && settings?.maxEvents && settings?.logDesc != null && verifyWebAppUrl(settings?.loggerAppUrl)) {
		state.optionsConfigured = true
	}	else {
		logDebug "${app.label} Unconfigured - Options. ${loggerUrlDev()} settings?.loggerAppUrl"
		state.optionsConfigured = false
	}
	
	if (settings?.allowedAttributes) {
		state.attributesConfigured = true
	}	else {
		logDebug "${app.label} Unconfigured - Choose Events"
	}
	
	if (getSelectedDevices()) {
		state.devicesConfigured = true
	}	else {
		logDebug "${app.label} Unconfigured - Choose Devices"
	}
	
	state.allConfigured = (state.apiKey && state.optionsConfigured && state.attributesConfigured && state.devicesConfigured)
	
	if  (state.allConfigured) {
		def logFrequency = (settings?.logFrequency ?: "10 Minutes").replace(" ", "")
		
		"runEvery${logFrequency}"(logNewEvents)
		
		//verifyGSVersion()
		runIn(10, startLogNewEvents)
	} else {
		logWarn "${app.label} Event Logging is disabled because there are unconfigured settings. ${settings?.loggerAppUrl}, ${settings?.apiKey}"
	}
}

private verifyWebAppUrl(url) {
	logDebug "${app.label} [verifyWebAppUrl] url, name: ${getWebAppName()},  logger: ${settings?.loggerAppUrl} "
	if (!url) {
		logDebug "${app.label} The ${getWebAppName()} Url field is required"
		return false
	}
	return true
/* 	} else if ("$url"?.toLowerCase()?.startsWith(loggerUrl) || "$url"?.toLowerCase()?.startsWith(loggerUrlDev)) {
		return true
	} else {		
		logWarn "The ${webAppName} Url is not valid"
		return false
	} */
}

// Requests the version from the Google Script and displays a warning if it's not the expected version.
private verifyGSVersion() {
	def actualGSVersion = ""
	
	logTrace "${app.label} Retrieving Google Script Code version of the ${getWebAppName()}"
	try {
		def params = [
			uri: settings?.loggerAppUrl
		]
	
		httpGet(params) { objResponse ->
			if (objResponse?.status == 200) {
				if ("${objResponse.data}" == "Version ${gsVersion()}") {
					logTrace "The ${getWebAppName()} is using the correct version of the Google Script code."
				}
				else {
					logWarn "The ${getWebAppName()} is not using version ${gsVersion()} of the Google Script code which is required by version ${version()} of the Simple Event Logger SmartApp.\n\nPlease update to the latest version of this SmartApp and the Google Script code to ensure that everything works properly.\n\nWhen deploying a new version of the Google Script Code in the Google Sheet, make sure you change the 'Product Version' field to 'New'."
				}
			}
			else {
				logWarn "Unable to connect to the ${getWebAppName()}.  Make sure you followed the instructions for setting up and testing it."
			}
		}
	}
	catch(e) {
		logWarn "Failed to retrieve Google Script Version.  Error: ${e.message}"
	}	
}

def startLogNewEvents() {
	logNewEvents()
}

def logNewEvents() {	
	def status = state.loggingStatus ?: [:]
	
	// Move the date range to the next position unless the google script failed last time or was skipped due to the sheet being archived.
	if (!status.success || status.eventsArchived) {
		status.lastEventTime = status.firstEventTime
	}
	
	status.success = null
	status.finished = null
	status.eventsArchived = null
	status.eventsLogged = 0
	status.started = new Date().time
	
	status.firstEventTime = getFirstEventTimeMS(status.lastEventTime)
	
	status.lastEventTime = getNewLastEventTimeMS(status.started, (status.firstEventTime + 1000))
	
	def startDate = new Date(status.firstEventTime + 1000)
	def endDate = new Date(status.lastEventTime)
	
	state.loggingStatus = status

	def events = getNewEvents(startDate, endDate)
	def eventCount = events?.size ?: 0
	def actionMsg = eventCount > 0 ? ", posting them to ${getWebAppName()}" : ""
	
	logDebug "${app.label} SmartThings found ${String.format('%,d', eventCount)} events between ${getFormattedLocalTime(startDate.time)} and ${getFormattedLocalTime(endDate.time)}${actionMsg}"
	
	if (events) {
		postEventsToLogger(events)
	}
	else {		
		state.loggingStatus.success = true
		state.loggingStatus.finished = new Date().time
	}
}

private getFirstEventTimeMS(lastEventTimeMS) {
	def firstRunMS = (3 * 60 * 60 * 1000) // 3 Hours 
	return safeToLong(lastEventTimeMS) ?: (new Date(new Date().time - firstRunMS)).time 
}

private getNewLastEventTimeMS(startedMS, firstEventMS) {
	if ((startedMS - firstEventMS) > logCatchUpFrequencySettingMS) {
		return (firstEventMS + logCatchUpFrequencySettingMS)
	}
	else {
		return startedMS
	}
}

private getLogCatchUpFrequencySetting() {
	return settings?.logCatchUpFrequency ?: "1 Hour"
}

private getLogCatchUpFrequencySettingMS() {
	def minutesVal
	switch (logCatchUpFrequencySetting) {
		case "15 Minutes":
			minutesVal = 15
			break
		case "30 Minutes":
			minutesVal = 30
			break
		case "1 Hour":
			minutesVal = 60
			break
		case "2 Hours":
			minutesVal = 120
			break
		case "6 Hours":
			minutesVal = 360
			break
		case "24 Hours":
			minutesVal = 1440
			break
		default:
			minutesVal = 60
	}
	return (minutesVal * 60 * 1000)
}

private postEventsToLogger(events) {
	def jsonOutput = new groovy.json.JsonOutput()
	def jsonData = jsonOutput.toJson([
		apiKey: settings?.apiKey,
		node: settings?.node,
		app: state.app,
		version: "${version()}",
		postBackUrl: "${state.endpoint}logger",
		archiveOptions: getArchiveOptions(),
		logDesc: (settings?.logDesc != false),
		logReporting: (settings?.logReporting == true),
		deleteExtraColumns: (settings?.deleteExtraColumns == true),
		events: events
	])

	def params = [
		//uri: "${settings?.googleWebAppUrl}",
		uri: "${settings?.loggerAppUrl}",
		contentType: "application/json",
		body: jsonData
	]	
	
	logTrace("[postEventsToLogger] params: ${params}")
	asynchttp_v1.post(processLogEventsResponse, params)
}

private getArchiveOptions() {
	return [
		logIsFull: (state.loggingStatus?.logIsFull ? true : false),
		type: (settings?.archiveType ?: ""),
		interval: safeToLong(settings?.archiveInterval, 50000)
	]
}

def processLogEventsResponse(response, data) {
	if (response?.status == 200) {
		//logTrace "${app.label} ${getWebAppName()} response.status: ${response.status}, response.data: ${response.data}"
		state.loggingStatus.success = true
		state.loggingStatus.finished = new Date().time
	} else if (response?.status == 301) {
		logWarn "${app.label} ${getWebAppName()} Response: ${response.status}, check your URL settings"
	} else if (response?.status == 302) {
		logWarn "${app.label} ${getWebAppName()} Response: ${response.status}, check your URL settings"
	}	else if (response?.status == 501) {
		logWarn "${app.label} Timeout while waiting for HundredGraphs"
	}	else {
		logWarn "${app.label} Unexpected response from ${getWebAppName()}: ${response.status}, ${response?.errorMessage}"
	}
	logTrace "${app.label} ${getWebAppName()} response.status: ${response.status}, response.data: ${response.data}"
	updateLoggingStatus(state, response)
}

private initializeAppEndpoint() {		
	try {
		if (!state.endpoint) {
			logDebug "${app.label} Creating Access Token"
			def accessToken = createAccessToken()
			if (accessToken) {
				state.endpoint = apiServerUrl("/api/token/${accessToken}/smartapps/installations/${app.id}/")
			}
		}		
	} 
	catch(e) {
		logWarn "${getInitializeEndpointErrorMessage()}"
		state.endpoint = null
	}
}

private getInitializeEndpointErrorMessage() {
	return "This SmartApp requires OAuth so please follow these steps to enable it:\n1.  Go into the My SmartApps section of the IDE\n2. Click the pencil icon next to this SmartApp to open the properties\n3.Click the 'OAuth' link\n4. Click 'Enable OAuth in Smart App'."
}

mappings {
	path("/update-logging-status") {
		action: [
			POST: "api_updateLoggingStatus"
		]
	}	
}

def updateLoggingStatus(state, response) {
	def status = state.loggingStatus ?: [:]
	def data = response.json
	
	//logTrace "${app.label} ${getWebAppName()} status: ${response.json}"
	
	if (data) {
		status.success = data.res
		//status.eventsArchived = data.eventsArchived
		//status.logIsFull = data.logIsFull
		//status.gsVersion = data.version
		status.finished = new Date().time
		status.eventsLogged = data.monitors
		//status.totalEventsLogged = data.totalEventsLogged
		//status.freeSpace = data.freeSpace
		
		if (data.error) {
			logDebug "${app.label} ${getWebAppName()} Reported: ${data.error}"
		}
	}
	else {
		status.success = false
		logDebug "${app.label} The ${getWebAppName()} postback has no data."
	}	
	state.loggingStatus = status
	logTrace "${app.label} [updateLoggingStatus] state: ${state}, status: ${status}"
	logLoggingStatus()
}

private logLoggingStatus() {
	def status = getFormattedLoggingStatus()
	if (status.logIsFull) {
		logWarn "${app.label} HG is Out of Space"
	}
	if (state.loggingStatus?.success) {
		if (status.eventsArchived) {
			logDebug "${app.label} ${getWebAppName()} archived events in ${status.runTime}."
		}
		else {
			logDebug "${app.label} ${getWebAppName()} logged ${status.eventsLogged} events between ${status.start} and ${status.end} in ${status.runTime}."			
		}		
	}
	else {
		logWarn "${app.label} ${getWebAppName()} failed to log events between ${status.start} and ${status.end}."
	}	
	
	//logTrace "HG hookVersion: ${state.loggingStatus?.hookVersion}, Total Events Logged: ${status.totalEventsLogged}, Used Space: ${status.usedSpace} records"
	//logTrace "HG hookVersion: ${state.loggingStatus?.hookVersion}, Used Space: ${status.usedSpace} records"
}

private getFormattedLoggingStatus() {
	def status = state.loggingStatus ?: [:]
	return [
		result: status?.success ? "Successful" : "Failed",
		start:  getFormattedLocalTime(safeToLong(status.firstEventTime)),
		end:  getFormattedLocalTime(safeToLong(status.lastEventTime)),
		runTime: "${((safeToLong(status.finished) - safeToLong(status.started)) / 1000)} seconds",
		eventsLogged: "${String.format('%,d', safeToLong(status.eventsLogged))}",
		totalEventsLogged: "${String.format('%,d', safeToLong(status.totalEventsLogged))}"
		//usedSpace: status.usedSpace
	]
}

private getNewEvents(startDate, endDate) {	
	def events = []
	
	getSelectedDevices()?.each  { device ->
		getDeviceAllowedAttrs(device?.displayName)?.each { attr ->
			//logTrace "checking device: ${device?.displayName} ${attr}"
			device.statesBetween("${attr}", startDate, endDate, [max: maxEventsSetting])?.each { event ->
				events << [
					time: event.date?.time,
					id: event.deviceId,
					device: device.displayName,
					type: "${attr}",
					value: event.value,
					desc: getEventDesc(event)
				]
			}
		}
	}
	logTrace "${app.label} Retrieving Events from ${startDate} to ${endDate} count: ${events?.size} - ${events}"
	return events?.unique()?.sort { it.time }
}

private getEventDesc(event) {
	if (settings?.useValueUnitDesc != false) {
		return "${event.value}" + (event.unit ? " ${event.unit}" : "")
	}
	else {
		def desc = "${event?.descriptionText}"
		if (desc.contains("{")) {
			desc = replaceToken(desc, "linkText", event.displayName)
			desc = replaceToken(desc, "displayName", event.displayName)
			desc = replaceToken(desc, "name", event.name)
			desc = replaceToken(desc, "value", event.value)
			desc = replaceToken(desc, "unit", event.unit)
		}
		return desc
	}
}

private replaceToken(desc, token, value) {
	desc = "$desc".replace("{{", "|").replace("}}", "|")
	return desc.replace("| ${token} |", "$value")
}

private getMaxEventsSetting() {
	return settings?.maxEvents ?: 200
}
	
private getFormattedLocalTime(utcTime) {
	if (utcTime) {
		try {
			def localTZ = TimeZone.getTimeZone(location.timeZone.ID)
			def localDate = new Date(utcTime + localTZ.getOffset(utcTime))	
			return localDate.format("MM/dd/yyyy HH:mm:ss")
		}
		catch (e) {
			logWarn "${app.label} Unable to get formatted local time for ${utcTime}: ${e.message}"
			return "${utcTime}"
		}
	}
	else {
		return ""
	}
}

private getFormattedUTCTime(utcTime) {
	if (utcTime) {
		try {
			def localTZ = TimeZone.getTimeZone(location.timeZone.ID)
			def localDate = new Date(utcTime)	
			return localDate.format("MM/dd/yyyy HH:mm:ss")
		}
		catch (e) {
			logWarn "${app.label} Unable to get formatted local time for ${utcTime}: ${e.message}"
			return "${utcTime}"
		}
	}
	else {
		return ""
	}
}

private getDeviceAllowedAttrs(deviceName) {
	def deviceAllowedAttrs = []
	try {
		settings?.allowedAttributes?.each { attr ->
			try {
				def attrExcludedDevices = settings?."${attr}Exclusions"
				
				if (!attrExcludedDevices?.find { it?.toLowerCase() == deviceName?.toLowerCase() }) {
					deviceAllowedAttrs << "${attr}"
				}
				//logTrace "${app.label} [retrieved] ${deviceName} id #${device?.displayName} for attr: ${attr}"
			}
			catch (e) {
				logWarn "${app.label} Error while getting device allowed attributes for ${device?.displayName} and attribute ${attr}: ${e.message}"
			}
		}
	}
	catch (e) {
		logWarn "${app.label} Error while getting device allowed attributes for ${device.displayName}: ${e.message}"
	}
	return deviceAllowedAttrs
}

private getSupportedAttributes() {
	def supportedAttributes = []
	def devices = getSelectedDevices()
	
	if (devices) {
	
		getAllAttributes()?.each { attr ->
			try {
				if (isAllDeviceAttr("$attr") || devices?.find { it?.hasAttribute("${attr}") }) {
					supportedAttributes << "${attr}"
				}
			}
			catch (e) {
				logWarn "${app.label} Error while finding supported devices for ${attr}: ${e.message}"
			}
			
		}
	}
	
	return supportedAttributes?.unique()?.sort()
}

private isAllDeviceAttr(attr) { 
	return getCapabilities().find { it.allDevices && it.attr == attr } ? true : false
}

private getAllAttributes() {
	def attributes = []	
	
	getCapabilities().each { cap ->
		try {		
			if (cap?.attr) {
				if (cap.attr instanceof Collection) {
					cap.attr.each { attr ->
						attributes << "${attr}"
					}
				}
				else {
					attributes << "${cap?.attr}"
				}
			}
		}
		catch (e) {
			logWarn "Error while getting attributes for capability ${cap}: ${e.message}"
		}
	}	
	return attributes
}

private getSelectedDeviceNames() {
	try {
		return getSelectedDevices()?.collect { it?.displayName }?.sort()
	}
	catch (e) {
		logWarn "Error while getting selected device names: ${e.message}"
		return []
	}
}

private getSelectedDevices() {
	def devices = []
	getCapabilities()?.each {	
		try {
			if (it.cap && settings?."${it.cap}Pref") {
				devices << settings?."${it.cap}Pref"
			}
		}
		catch (e) {
			logWarn "Error while getting selected devices for capability ${it}: ${e.message}"
		}
	}	
	return devices?.flatten()?.unique { it.displayName }
}

private getCapabilities() {
	[
		[title: "Actuators", cap: "actuator"],
		[title: "Sensors", cap: "sensor"],
		[title: "Acceleration Sensors", cap: "accelerationSensor", attr: "acceleration"],
		[title: "Device Activity", attr: "activity", allDevices: true],
		[title: "Alarms", cap: "alarm", attr: "alarm"],
		[title: "Batteries", cap: "battery", attr: "battery"],
		[title: "Beacons", cap: "beacon", attr: "presence"],
		[title: "Bulbs", cap: "bulb", attr: "switch"],
		[title: "Buttons", cap: "button", attr: ["button", "numberOfButtons"]],
		[title: "Carbon Dioxide Measurement Sensors", cap: "carbonDioxideMeasurement", attr: "carbonDioxide"],
		[title: "Carbon Monoxide Detectors", cap: "carbonMonoxideDetector", attr: "carbonMonoxide"],
		[title: "Color Control Devices", cap: "colorControl", attr: ["color", "hue", "saturation"]],
		[title: "Color Temperature Devices", cap: "colorTemperature", attr: "colorTemperature"],
		[title: "Consumable Devices", cap: "consumable", attr: "consumableStatus"],
		[title: "Contact Sensors", cap: "contactSensor", attr: "contact"],
		[title: "Doors", cap: "doorControl", attr: "door"],
		[title: "Energy Meters", cap: "energyMeter", attr: "energy"],
		[title: "Garage Doors", cap: "garageDoorControl", attr: "door"],
		[title: "Illuminance Measurement Sensors", cap: "illuminanceMeasurement", attr: "illuminance"],
		[title: "Image Capture Devices", cap: "imageCapture", attr: "image"],		
		[title: "Indicators", cap: "indicator", attr: "indicatorStatus"],
		[title: "Lights", cap: "light", attr: "switch"],
		[title: "Locks", cap: "lock", attr: "lock"],
		[title: "Media Controllers", cap: "mediaController", attr: "currentActivity"],
		[title: "Motion Sensors", cap: "motionSensor", attr: "motion"],
		[title: "Music Players", cap: "musicPlayer", attr: ["level", "mute", "status", "trackDescription"]],
		[title: "Outlets", cap: "outlet", attr: "switch"],
		[title: "pH Measurement Sensors", cap: "phMeasurement", attr: "pH"],
		[title: "Power Meters", cap: "powerMeter", attr: "power"],
		[title: "Power Sources", cap: "powerSource", attr: "powerSource"],
		[title: "Presence Sensors", cap: "presenceSensor", attr: "presence"],
		[title: "Relative Humidity Measurement Sensors", cap: "relativeHumidityMeasurement", attr: "humidity"],
		[title: "Relay Switches", cap: "relaySwitch", attr: "switch"],
		[title: "Shock Sensors", cap: "shockSensor", attr: "shock"],
		[title: "Signal Strength Sensors", cap: "signalStrength", attr: ["lqi", "rssi"]],
		[title: "Sleep Sensors", cap: "sleepSensor", attr: "sleeping"],
		[title: "Smoke Detectors", cap: "smokeDetector", attr: "smoke"],
		[title: "Sound Pressure Level Sensors", cap: "soundPressureLevel", attr: "soundPressureLevel"],
		[title: "Sound Sensors", cap: "soundSensor", attr: "sound"],
		[title: "Speech Recognition Sensors", cap: "speechRecognition", attr: "phraseSpoken"],
		[title: "Switches", cap: "switch", attr: "switch"],
		[title: "Switch Level Sensors", cap: "switchLevel", attr: "level"],
		[title: "Tamper Alert Sensors", cap: "tamperAlert", attr: "tamper"],
		[title: "Temperature Measurement Sensors", cap: "temperatureMeasurement", attr: "temperature"],
		[title: "Thermostats", cap: "thermostat", attr: ["coolingSetpoint", "heatingSetpoint", "temperature", "thermostatFanMode", "thermostatMode", "thermostatOperatingState", "thermostatSetpoint"]],
		[title: "Three Axis Sensors", cap: "threeAxis", attr: "threeAxis"],
		[title: "Touch Sensors", cap: "touchSensor", attr: "touch"],
		[title: "Ultraviolet Index Sensors", cap: "ultravioletIndex", attr: "ultravioletIndex"],
		[title: "Valves", cap: "valve", attr: "valve"],
		[title: "Voltage Measurement Sensors", cap: "voltageMeasurement", attr: "voltage"],
		[title: "Water Sensors", cap: "waterSensor", attr: "water"],
		[title: "Window Shades", cap: "windowShade", attr: "windowShade"]
	]
}

// private averageSupportedAttributes() {
	// [
		// "battery",
		// "carbonDioxide",
		// "colorTemperature",
		// "coolingSetpoint",
		// "energy",
		// "heatingSetpoint",
		// "humidity",
		// "illuminance",
		// "level",
		// "lqi",
		// "pH",
		// "power",
		// "rssi",
		// "soundPressureLevel",
		// "temperature",
		// "thermostatSetpoint",
		// "ultravioletIndex",
		// "voltage"
	// ]
// }

private getArchiveTypeOptions() {
	[
		[name: "None"],
		[name: "Out of Space"],
		[name: "Weeks", defaultVal: 2, range: "1..52"],
		[name: "Events", defaultVal: 25000, range: "1000..100000"]
	]
}


private getWebAppName() {
	return "HundredGraph Logger"
}

private loggerUrl() {
	return "https://www.hundredgraphs.com/hook/"
}
private loggerUrlDev() {
	return "http://dev.hundredgraphs.com/hook/"
}

private getWebAppBaseUrl() {
	return "https://www.hundredgraphs.com/hook/"
	//return "https://script.google.com/macros/s/"
}

private getWebAppBaseUrl2() {
	return "https://script.google.com/macros/u/"
}

long safeToLong(val, defaultVal=0) {
	try {
		if (val && (val instanceof Long || "${val}".isLong())) {
			return "$val".toLong()
		}
		else {
			return defaultVal
		}
	}
	catch (e) {
		return defaultVal
	}
}

private logDebug(msg) {
	if (loggingTypeEnabled("debug")) {
		log.debug msg
	}
}

private logTrace(msg) {
	if (loggingTypeEnabled("trace")) {
		log.trace msg
	}
}

private logInfo(msg) {
	if (loggingTypeEnabled("info")) {
		log.info msg
	}
}

private logWarn(msg) {
	log.warn msg
}

private loggingTypeEnabled(loggingType) {
	return (!settings?.logging || settings?.logging?.contains(loggingType))
}