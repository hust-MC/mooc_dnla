package com.max.dlna

import android.util.Log
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.xml.sax.XMLReader
import javax.xml.parsers.SAXParserFactory

class CustomAVTransportLastChangeParser : AVTransportLastChangeParser() {
    override fun getSchemaSources() = null

    override fun create(): XMLReader {
        val factory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = true

        // 尝试设置安全特性（如果支持）
        trySetSecurityFeature(factory)

        val reader = factory.newSAXParser().xmlReader

        // 尝试在XMLReader上设置安全特性（如果支持）
        trySetSecurityFeature(reader)
        return reader
    }

    /**
     * 尝试在SAXParserFactory上设置安全特性
     */
    private fun trySetSecurityFeature(factory: SAXParserFactory) {
        try {
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        } catch (e: Exception) {
            Log.w(TAG, "创建XML解析器失败：${e.message}")
        }
    }

    /**
     * 尝试在SAXParserFactory上设置安全特性
     */
    private fun trySetSecurityFeature(reader: XMLReader) {
        try {
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false)
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        } catch (e: Exception) {
            Log.w(TAG, "创建XML解析器失败：${e.message}")
        }
    }

    companion object {
        const val TAG = "CustomAVTransportLastChangeParser"
    }
}