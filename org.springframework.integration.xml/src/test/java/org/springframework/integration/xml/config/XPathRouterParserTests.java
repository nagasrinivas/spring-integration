/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.xml.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.xml.router.XPathSingleChannelNameResolver;
import org.springframework.integration.xml.util.XmlTestUtil;
import org.springframework.test.context.ContextConfiguration;
import org.w3c.dom.Document;

/**
 * @author Jonas Partner
 */
@ContextConfiguration
public class XPathRouterParserTests {

	@Test
	public void testSimpleStringExpression() throws Exception {
		Document doc = XmlTestUtil.getDocumentForString("<name>outputOne</name>");
		GenericMessage<Document> docMessage = new GenericMessage<Document>(doc);

		TestXmlApplicationContext ctx = TestXmlApplicationContextHelper.getTestAppContext("<si-xml:xpath-router id='router' xpath-expression='/name' />");
		XPathSingleChannelNameResolver router = (XPathSingleChannelNameResolver) ctx.getBean("router");

		String[] channelNames = router.resolveChannelNames(docMessage);
		assertEquals("Wrong number of channel names returned", 1, channelNames.length);
		assertEquals("Wrong channel name", "outputOne", channelNames[0]);

	}

	@Test
	public void testNamespacedStringExpression() throws Exception {
		Document doc = XmlTestUtil.getDocumentForString("<ns1:name xmlns:ns1='www.example.org'>outputOne</ns1:name>");
		GenericMessage<Document> docMessage = new GenericMessage<Document>(doc);

		TestXmlApplicationContext ctx = TestXmlApplicationContextHelper.getTestAppContext("<si-xml:xpath-router id='router' xpath-expression='/ns2:name' ns-prefix='ns2' ns-uri='www.example.org' />");
		XPathSingleChannelNameResolver router = (XPathSingleChannelNameResolver) ctx.getBean("router");

		String[] channelNames = router.resolveChannelNames(docMessage);
		assertEquals("Wrong number of channel names returned", 1, channelNames.length);
		assertEquals("Wrong channel name", "outputOne", channelNames[0]);
	}

	@Test
	public void testStringExpressionWithNestedNamespaceMap() throws Exception {
		Document doc = XmlTestUtil
				.getDocumentForString("<ns1:name xmlns:ns1='www.example.org' xmlns:ns2='www.example.org2'><ns2:type>outputOne</ns2:type></ns1:name>");
		GenericMessage<Document> docMessage = new GenericMessage<Document>(doc);

		StringBuffer buffer = new StringBuffer(
				"<si-xml:xpath-router id='router' xpath-expression='/ns1:name/ns2:type' >");
		buffer
				.append("<map><entry key='ns1' value='www.example.org' /> <entry key='ns2' value='www.example.org2'/></map>");
		buffer.append("</si-xml:xpath-router>");

		TestXmlApplicationContext ctx = TestXmlApplicationContextHelper.getTestAppContext(buffer.toString());
		XPathSingleChannelNameResolver router = (XPathSingleChannelNameResolver) ctx.getBean("router");

		String[] channelNames = router.resolveChannelNames(docMessage);
		assertEquals("Wrong number of channel names returned", 1, channelNames.length);
		assertEquals("Wrong channel name", "outputOne", channelNames[0]);
	}

	@Test
	public void testStringExpressionWithReferenceToNamespaceMap() throws Exception {
		Document doc = XmlTestUtil
				.getDocumentForString("<ns1:name xmlns:ns1='www.example.org' xmlns:ns2='www.example.org2'><ns2:type>outputOne</ns2:type></ns1:name>");
		GenericMessage<Document> docMessage = new GenericMessage<Document>(doc);
		StringBuffer buffer = new StringBuffer(
				"<si-xml:xpath-router id='router' xpath-expression='/ns1:name/ns2:type' namespace-map='nsMap'>");
		buffer
				.append("</si-xml:xpath-router>")
				.append("<util:map id='nsMap'><entry key='ns1' value='www.example.org' /><entry key='ns2' value='www.example.org2' /></util:map>");

		TestXmlApplicationContext ctx = TestXmlApplicationContextHelper.getTestAppContext(buffer.toString());
		XPathSingleChannelNameResolver router = (XPathSingleChannelNameResolver) ctx.getBean("router");

		String[] channelNames = router.resolveChannelNames(docMessage);
		assertEquals("Wrong number of channel names returned", 1, channelNames.length);
		assertEquals("Wrong channel name", "outputOne", channelNames[0]);
	}


	@Test(expected = Exception.class)
	public void testNamespacedWithNoPrefixStringExpression() throws Exception {
		TestXmlApplicationContextHelper.getTestAppContext("<si-xml:xpath-router id='router' xpath-expression='/ns2:name' xpath-namespace='www.example.org' />");
	}

	@Test(expected = Exception.class)
	public void testPrefixWithNoNamespaceStringExpression() throws Exception {
		TestXmlApplicationContextHelper.getTestAppContext("<si-xml:xpath-router id='router' xpath-expression='/ns2:name' xpath-prefix='ns2' />");
	}

}
