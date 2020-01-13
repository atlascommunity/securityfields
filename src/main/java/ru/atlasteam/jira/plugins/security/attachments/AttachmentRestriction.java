package ru.atlasteam.jira.plugins.security.attachments;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("AttachRestriction")
public interface AttachmentRestriction extends Entity {
    long getAttachmentId();
    void setAttachmentId(long attachmentId);

    String getGroupName();
    void setGroupName(String groupName);

    Long getProjectRoleId();
    void setProjectRoleId(Long projectRoleId);
}
