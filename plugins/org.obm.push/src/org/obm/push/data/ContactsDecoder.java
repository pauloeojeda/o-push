package org.obm.push.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.IApplicationData;
import org.obm.push.impl.SyncHandler;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

// Nouveau contact
//<Commands>
//<Add>
//<ClientId>2147483657</ClientId>
//<ApplicationData>
//<FileAs>Tttt</FileAs>
//<FirstName>Tttt</FirstName>
//<Picture/>
//</ApplicationData>
//</Add>
//</Commands>


public class ContactsDecoder extends Decoder implements IDataDecoder {

	private static final Log logger = LogFactory.getLog(SyncHandler.class);

	@Override
	public IApplicationData decode(Element syncData) {
		Element element;
		Contact contact = new Contact();
		
		contact.setAssistantName(parseDOMString(DOMUtils.getUniqueElement(syncData, "AssistantName")));
		contact.setAssistantPhoneNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "AssistantPhoneNumber")));
		contact.setAssistnamePhoneNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "AssistnamePhoneNumber")));
		contact.setBusiness2PhoneNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "Business2PhoneNumber")));
		contact.setBusinessAddressCity(parseDOMString(DOMUtils.getUniqueElement(syncData, "BusinessAddressCity")));
		contact.setBusinessPhoneNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "BusinessPhoneNumber")));
		contact.setWebPage(parseDOMString(DOMUtils.getUniqueElement(syncData, "WebPage")));
		contact.setBusinessAddressCountry(parseDOMString(DOMUtils.getUniqueElement(syncData, "BusinessAddressCountry")));
		contact.setDepartment(parseDOMString(DOMUtils.getUniqueElement(syncData, "Department")));
		contact.setEmail1Address(parseDOMString(DOMUtils.getUniqueElement(syncData, "Email1Address")));
		contact.setEmail2Address(parseDOMString(DOMUtils.getUniqueElement(syncData, "Email2Address")));
		contact.setEmail3Address(parseDOMString(DOMUtils.getUniqueElement(syncData, "Email3Address")));
		contact.setBusinessFaxNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "BusinessFaxNumber")));
		contact.setFileAs(parseDOMString(DOMUtils.getUniqueElement(syncData, "FileAs")));
		contact.setFirstName(parseDOMString(DOMUtils.getUniqueElement(syncData, "FirstName")));
		contact.setMiddleName(parseDOMString(DOMUtils.getUniqueElement(syncData, "MiddleName")));
		contact.setHomeAddressCity(parseDOMString(DOMUtils.getUniqueElement(syncData, "HomeAddressCity")));
		contact.setHomeAddressCountry(parseDOMString(DOMUtils.getUniqueElement(syncData, "HomeAddressCountry")));
		contact.setHomeFaxNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "HomeFaxNumber")));
		contact.setHomePhoneNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "HomePhoneNumber")));
		contact.setHome2PhoneNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "Home2PhoneNumber")));
		contact.setHomeAddressPostalCode(parseDOMString(DOMUtils.getUniqueElement(syncData, "HomeAddressPostalCode")));
		contact.setHomeAddressState(parseDOMString(DOMUtils.getUniqueElement(syncData, "HomeAddressState")));
		contact.setHomeAddressStreet(parseDOMString(DOMUtils.getUniqueElement(syncData, "HomeAddressStreet")));
		contact.setMobilePhoneNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "MobilePhoneNumber")));
		contact.setSuffix(parseDOMString(DOMUtils.getUniqueElement(syncData, "Suffix")));
		contact.setCompanyName(parseDOMString(DOMUtils.getUniqueElement(syncData, "CompanyName")));
		contact.setOtherAddressCity(parseDOMString(DOMUtils.getUniqueElement(syncData, "OtherAddressCity")));
		contact.setOtherAddressCountry(parseDOMString(DOMUtils.getUniqueElement(syncData, "OtherAddressCountry")));
		contact.setCarPhoneNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "CarPhoneNumber")));
		contact.setOtherAddressPostalCode(parseDOMString(DOMUtils.getUniqueElement(syncData, "OtherAddressPostalCode")));
		contact.setOtherAddressState(parseDOMString(DOMUtils.getUniqueElement(syncData, "OtherAddressState")));
		contact.setOtherAddressStreet(parseDOMString(DOMUtils.getUniqueElement(syncData, "OtherAddressStreet")));
		contact.setPagerNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "PagerNumber")));
		contact.setTitle(parseDOMString(DOMUtils.getUniqueElement(syncData, "Title")));
		contact.setBusinessPostalCode(parseDOMString(DOMUtils.getUniqueElement(syncData, "BusinessPostalCode")));
		contact.setLastName(parseDOMString(DOMUtils.getUniqueElement(syncData, "LastName")));
		contact.setSpouse(parseDOMString(DOMUtils.getUniqueElement(syncData, "Spouse")));
		contact.setBusinessState(parseDOMString(DOMUtils.getUniqueElement(syncData, "BusinessState")));
		contact.setBusinessStreet(parseDOMString(DOMUtils.getUniqueElement(syncData, "BusinessStreet")));
		contact.setJobTitle(parseDOMString(DOMUtils.getUniqueElement(syncData, "JobTitle")));
		contact.setYomiFirstName(parseDOMString(DOMUtils.getUniqueElement(syncData, "YomiFirstName")));
		contact.setYomiLastName(parseDOMString(DOMUtils.getUniqueElement(syncData, "YomiLastName")));
		contact.setYomiCompanyName(parseDOMString(DOMUtils.getUniqueElement(syncData, "YomiCompanyName")));
		contact.setOfficeLocation(parseDOMString(DOMUtils.getUniqueElement(syncData, "OfficeLocation")));
		contact.setRadioPhoneNumber(parseDOMString(DOMUtils.getUniqueElement(syncData, "RadioPhoneNumber")));
		contact.setPicture(parseDOMString(DOMUtils.getUniqueElement(syncData, "Picture")));
		contact.setAnniversary(parseDOMDate(DOMUtils.getUniqueElement(syncData, "Anniversary")));
		contact.setBirthday(parseDOMDate(DOMUtils.getUniqueElement(syncData, "Birthday")));
		
		contact.setCategories(parseDOMStringCollection(DOMUtils.getUniqueElement(syncData, "Categories"), "Category"));
		contact.setChildren(parseDOMStringCollection(DOMUtils.getUniqueElement(syncData, "Children"), "Child"));
		
		
		// Contacts2
		
		contact.setCustomerId(parseDOMString(DOMUtils.getUniqueElement(syncData, "CustomerId")));
		contact.setGovernmentId(parseDOMString(DOMUtils.getUniqueElement(syncData, "GovernmentId")));
		contact.setIMAddress(parseDOMString(DOMUtils.getUniqueElement(syncData, "IMAddress")));
		contact.setIMAddress2(parseDOMString(DOMUtils.getUniqueElement(syncData, "IMAddress2")));
		contact.setIMAddress3(parseDOMString(DOMUtils.getUniqueElement(syncData, "IMAddress3")));
		contact.setManagerName(parseDOMString(DOMUtils.getUniqueElement(syncData, "ManagerName")));
		contact.setCompanyMainPhone(parseDOMString(DOMUtils.getUniqueElement(syncData, "CompanyMainPhone")));
		contact.setAccountName(parseDOMString(DOMUtils.getUniqueElement(syncData, "AccountName")));
		contact.setNickName(parseDOMString(DOMUtils.getUniqueElement(syncData, "NickName")));
		contact.setMMS(parseDOMString(DOMUtils.getUniqueElement(syncData, "MMS")));
		
		return null;
	}
}
