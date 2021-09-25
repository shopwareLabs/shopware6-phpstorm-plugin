package de.shyim.shopware6.xml

import com.intellij.codeInsight.daemon.impl.analysis.XmlHighlightVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.xml.XmlDescriptorUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor
import de.shyim.shopware6.index.dict.AdminComponent

class AdminComponentTagDescriptor(
    private val component: AdminComponent,
    private val xmlTag: XmlTag,
    private val attributes: Set<String>
) : XmlElementDescriptor {
    override fun getDeclaration() = xmlTag

    override fun getName(context: PsiElement?) = component.name

    override fun getName(): String = component.name

    override fun init(element: PsiElement?) {}

    override fun getQualifiedName() = component.name

    override fun getDefaultName() = component.name

    private fun disableValidation(xmlTag: XmlTag?) {
        xmlTag ?: return
        XmlHighlightVisitor.setSkipValidation(xmlTag)
    }

    override fun getElementsDescriptors(context: XmlTag?): Array<XmlElementDescriptor> {
        disableValidation(context)
        return XmlDescriptorUtil.getElementsDescriptors(context)
    }

    override fun getElementDescriptor(childTag: XmlTag?, contextTag: XmlTag?): XmlElementDescriptor? {
        disableValidation(contextTag)
        return XmlDescriptorUtil.getElementDescriptor(childTag, contextTag)
    }

    override fun getAttributesDescriptors(context: XmlTag?): Array<XmlAttributeDescriptor> {
        if (context == null) {
            return arrayOf()
        }

        val propsSet = HashSet<XmlAttributeDescriptor>()

        attributes.forEach {
            propsSet.add(AdminComponentAttributeDescriptor(it, context))
        }

        return propsSet.toTypedArray()
    }

    override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor {
        return AnyXmlAttributeDescriptor(attributeName)
    }

    override fun getAttributeDescriptor(attribute: XmlAttribute): XmlAttributeDescriptor {
        return getAttributeDescriptor(attribute.name, attribute.parent)
    }

    override fun getNSDescriptor(): Nothing? = null

    override fun getTopGroup(): Nothing? = null

    override fun getContentType(): Int = XmlElementDescriptor.CONTENT_TYPE_ANY

    override fun getDefaultValue(): String? = null
}