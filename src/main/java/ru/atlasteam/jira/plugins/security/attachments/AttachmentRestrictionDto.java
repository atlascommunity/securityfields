package ru.atlasteam.jira.plugins.security.attachments;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@XmlRootElement
public class AttachmentRestrictionDto {
    @XmlElement
    private long attachmentId;
    @XmlElement
    private String restrictionName;
    @XmlElement
    private boolean allowedToDownload;
    @XmlElement
    private boolean allowedToRestrict;

    public AttachmentRestrictionDto(long attachmentId, String restrictionName, boolean allowedToDownload, boolean allowedToRestrict) {
        this.attachmentId = attachmentId;
        this.restrictionName = restrictionName;
        this.allowedToDownload = allowedToDownload;
        this.allowedToRestrict = allowedToRestrict;
    }
}
