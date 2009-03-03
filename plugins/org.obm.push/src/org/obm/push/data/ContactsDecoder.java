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


public class ContactsDecoder implements IDataDecoder {

	private static final Log logger = LogFactory.getLog(SyncHandler.class);

	@Override
	public IApplicationData decode(Element syncData) {
		Element element;
		Contact contact = new Contact();
		
		// Strings
		
		element = DOMUtils.getUniqueElement(syncData, "AssistantName");
		if (element != null) contact.setAssistantName(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "AssistantPhoneNumber");
		if (element != null) contact.setAssistantPhoneNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "AssistnamePhoneNumber");
		if (element != null) contact.setAssistnamePhoneNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "Business2PhoneNumber");
		if (element != null) contact.setBusiness2PhoneNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "BusinessAddressCity");
		if (element != null) contact.setBusinessAddressCity(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "BusinessPhoneNumber");
		if (element != null) contact.setBusinessPhoneNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "WebPage");
		if (element != null) contact.setWebPage(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "BusinessAddressCountry");
		if (element != null) contact.setBusinessAddressCountry(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "Department");
		if (element != null) contact.setDepartment(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "Email1Address");
		if (element != null) contact.setEmail1Address(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "Email2Address");
		if (element != null) contact.setEmail2Address(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "Email3Address");
		if (element != null) contact.setEmail3Address(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "BusinessFaxNumber");
		if (element != null) contact.setBusinessFaxNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "FileAs");
		if (element != null) contact.setFileAs(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "FirstName");
		if (element != null) contact.setFirstName(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "MiddleName");
		if (element != null) contact.setMiddleName(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "HomeAddressCity");
		if (element != null) contact.setHomeAddressCity(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "HomeAddressCountry");
		if (element != null) contact.setHomeAddressCountry(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "HomeFaxNumber");
		if (element != null) contact.setHomeFaxNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "HomePhoneNumber");
		if (element != null) contact.setHomePhoneNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "Home2PhoneNumber");
		if (element != null) contact.setHome2PhoneNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "HomeAddressPostalCode");
		if (element != null) contact.setHomeAddressPostalCode(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "HomeAddressState");
		if (element != null) contact.setHomeAddressState(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "HomeAddressStreet");
		if (element != null) contact.setHomeAddressStreet(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "MobilePhoneNumber");
		if (element != null) contact.setMobilePhoneNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "Suffix");
		if (element != null) contact.setSuffix(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "CompanyName");
		if (element != null) contact.setCompanyName(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "OtherAddressCity");
		if (element != null) contact.setOtherAddressCity(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "OtherAddressCountry");
		if (element != null) contact.setOtherAddressCountry(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "CarPhoneNumber");
		if (element != null) contact.setCarPhoneNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "OtherAddressPostalCode");
		if (element != null) contact.setOtherAddressPostalCode(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "OtherAddressState");
		if (element != null) contact.setOtherAddressState(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "OtherAddressStreet");
		if (element != null) contact.setOtherAddressStreet(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "PagerNumber");
		if (element != null) contact.setPagerNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "Title");
		if (element != null) contact.setTitle(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "BusinessPostalCode");
		if (element != null) contact.setBusinessPostalCode(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "LastName");
		if (element != null) contact.setLastName(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "Spouse");
		if (element != null) contact.setSpouse(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "BusinessState");
		if (element != null) contact.setBusinessState(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "BusinessStreet");
		if (element != null) contact.setBusinessStreet(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "JobTitle");
		if (element != null) contact.setJobTitle(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "YomiFirstName");
		if (element != null) contact.setYomiFirstName(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "YomiLastName");
		if (element != null) contact.setYomiLastName(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "YomiCompanyName");
		if (element != null) contact.setYomiCompanyName(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "OfficeLocation");
		if (element != null) contact.setOfficeLocation(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "RadioPhoneNumber");
		if (element != null) contact.setRadioPhoneNumber(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "Picture");
		if (element != null) contact.setPicture(element.getTextContent());
		
		// Dates
		
		element = DOMUtils.getUniqueElement(syncData, "Anniversary");
		if (element != null) contact.setAnniversary(parseDate(element.getTextContent()));
		
		element = DOMUtils.getUniqueElement(syncData, "Birthday");
		if (element != null) contact.setBirthday(parseDate(element.getTextContent()));

		// Collections
		
		element = DOMUtils.getUniqueElement(syncData, "Categories");
		if (element != null) contact.setCategories(parseStringCollection(element, "Category"));
		
		element = DOMUtils.getUniqueElement(syncData, "Children");
		if (element != null) contact.setChildren(parseStringCollection(element, "Child"));
		
		
		// Contacts2
		
		element = DOMUtils.getUniqueElement(syncData, "CustomerId");
		if (element != null) contact.setCustomerId(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "GovernmentId");
		if (element != null) contact.setGovernmentId(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "IMAddress");
		if (element != null) contact.setIMAddress(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "IMAddress2");
		if (element != null) contact.setIMAddress2(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "IMAddress3");
		if (element != null) contact.setIMAddress3(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "ManagerName");
		if (element != null) contact.setManagerName(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "CompanyMainPhone");
		if (element != null) contact.setCompanyMainPhone(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "AccountName");
		if (element != null) contact.setAccountName(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "NickName");
		if (element != null) contact.setNickName(element.getTextContent());

		element = DOMUtils.getUniqueElement(syncData, "MMS");
		if (element != null) contact.setMMS(element.getTextContent());
		
		return null;
	}
	
	public SimpleDateFormat parseDate (String str) {
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'");
		try {
			date.setTimeZone(TimeZone.getTimeZone("GMT"));
			date.parse(str);
			
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public ArrayList<String> parseStringCollection(Element node, String elementName) {
		return new ArrayList<String>(Arrays.asList(DOMUtils.getTexts(node, elementName)));
	}
}
