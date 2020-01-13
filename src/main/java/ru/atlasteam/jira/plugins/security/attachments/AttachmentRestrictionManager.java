package ru.atlasteam.jira.plugins.security.attachments;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.sal.api.transaction.TransactionCallback;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import ru.atlasteam.jira.plugins.security.common.RestExecutor;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/attachmentrestriction")
@Produces({MediaType.APPLICATION_JSON})
public class AttachmentRestrictionManager {
    private final ActiveObjects ao;
    private final AttachmentManager attachmentManager;
    private final GroupManager groupManager;
    private final I18nHelper i18nHelper;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ProjectRoleManager projectRoleManager;

    public AttachmentRestrictionManager(ActiveObjects ao, AttachmentManager attachmentManager, GroupManager groupManager, I18nHelper i18nHelper, IssueManager issueManager, JiraAuthenticationContext jiraAuthenticationContext, ProjectRoleManager projectRoleManager) {
        this.ao = ao;
        this.attachmentManager = attachmentManager;
        this.groupManager = groupManager;
        this.i18nHelper = i18nHelper;
        this.issueManager = issueManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.projectRoleManager = projectRoleManager;
    }

    private AttachmentRestriction getAttachmentRestriction(final long attachmentId) {
        return ao.executeInTransaction(new TransactionCallback<AttachmentRestriction>() {
            @Override
            public AttachmentRestriction doInTransaction() {
                AttachmentRestriction[] attachmentRestrictions = ao.find(AttachmentRestriction.class, Query.select().where("ATTACHMENT_ID = ?", attachmentId));
                if (attachmentRestrictions.length == 0)
                    return null;
                if (attachmentRestrictions.length > 1)
                    throw new IllegalStateException(String.format("Several attachment restrictions with attachment id %s", attachmentId));
                return attachmentRestrictions[0];
            }
        });
    }

    public void updateAttachmentRestriction(long attachmentId, String restriction) throws Exception {
        if (StringUtils.isNotEmpty(restriction)) {
            String[] splitRestriction = restriction.split(":");
            if (splitRestriction[0].equals("group"))
                updateAttachmentRestriction(attachmentId, splitRestriction[1], null);
            else if (splitRestriction[0].equals("role"))
                updateAttachmentRestriction(attachmentId, null, Long.parseLong(splitRestriction[1]));
            else
                throw new IllegalArgumentException(String.format("Illegal restriction value: %s", restriction));
        }
    }

    private AttachmentRestriction updateAttachmentRestriction(final long attachmentId, final String groupName, final Long projectRoleId) throws Exception {
        return ao.executeInTransaction(new TransactionCallback<AttachmentRestriction>() {
            @Override
            public AttachmentRestriction doInTransaction() {
                AttachmentRestriction attachmentRestriction = getAttachmentRestriction(attachmentId);
                if (attachmentRestriction == null) {
                    attachmentRestriction = ao.create(AttachmentRestriction.class);
                    attachmentRestriction.setAttachmentId(attachmentId);
                }
                attachmentRestriction.setGroupName(groupName);
                attachmentRestriction.setProjectRoleId(projectRoleId);
                attachmentRestriction.save();
                return attachmentRestriction;
            }
        });
    }

    private void deleteAttachmentRestriction(final long attachmentId) {
        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                AttachmentRestriction attachmentRestriction = getAttachmentRestriction(attachmentId);
                if (attachmentRestriction != null)
                    ao.delete(attachmentRestriction);
                return null;
            }
        });
    }

    public boolean isAllowedToDownload(long attachmentId) {
        AttachmentRestriction attachmentRestriction = getAttachmentRestriction(attachmentId);
        if (attachmentRestriction == null)
            return true;

        ApplicationUser user = jiraAuthenticationContext.getUser();
        if (user == null)
            return false;

        String groupName = attachmentRestriction.getGroupName();
        if (groupName != null) {
            Group group = groupManager.getGroup(groupName);
            return group != null && groupManager.isUserInGroup(user, group);
        }

        Long projectRoleId = attachmentRestriction.getProjectRoleId();
        if (projectRoleId != null) {
            ProjectRole projectRole = projectRoleManager.getProjectRole(projectRoleId);
            Project project = attachmentManager.getAttachment(attachmentId).getIssueObject().getProjectObject();
            return projectRole != null && projectRoleManager.isUserInProjectRole(user, projectRole, project);
        }

        return false;
    }

    private boolean isAllowedToRestrict(long attachmentId) {
        ApplicationUser attachmentAuthor = attachmentManager.getAttachment(attachmentId).getAuthorObject();
        return attachmentAuthor != null && attachmentAuthor.equals(jiraAuthenticationContext.getUser());
    }

    public String getAttachmentRestrictionName(long attachmentId) {
        AttachmentRestriction attachmentRestriction = getAttachmentRestriction(attachmentId);
        if (attachmentRestriction != null) {
            String groupName = attachmentRestriction.getGroupName();
            if (groupName != null)
                return groupName;

            Long projectRoleId = attachmentRestriction.getProjectRoleId();
            if (projectRoleId != null) {
                ProjectRole projectRole = projectRoleManager.getProjectRole(projectRoleId);
                if (projectRole != null)
                    return projectRole.getName();
            }
        }
        return null;
    }

    @RequiresXsrfCheck
    @POST
    @Path("/{attachmentId}")
    public Response update(@PathParam("attachmentId") final long attachmentId,
                           @FormParam("restriction") final String restriction) {
        return new RestExecutor<Void>() {
            @Override
            protected Void doAction() throws Exception {
                if (!isAllowedToRestrict(attachmentId))
                    throw new SecurityException();

                updateAttachmentRestriction(attachmentId, restriction);
                return null;
            }
        }.getResponse();
    }

    @RequiresXsrfCheck
    @DELETE
    @Path("/{attachmentId}")
    public Response delete(@PathParam("attachmentId") final long attachmentId) {
        return new RestExecutor<Void>() {
            @Override
            protected Void doAction() throws Exception {
                if (!isAllowedToRestrict(attachmentId))
                    throw new SecurityException();

                deleteAttachmentRestriction(attachmentId);
                return null;
            }
        }.getResponse();
    }

    @GET
    @Path("/issue/{issueKey}")
    public Response checkAttachmentRestrictions(@PathParam("issueKey") final String issueKey) {
        return new RestExecutor<List<AttachmentRestrictionDto>>() {
            @Override
            protected List<AttachmentRestrictionDto> doAction() throws Exception {
                Issue issue = issueManager.getIssueObject(issueKey);
                if (issue == null)
                    throw new NullPointerException(i18nHelper.getText("rest.issue.link.error.issue.key", issueKey));
                List<Attachment> attachments = attachmentManager.getAttachments(issue);

                List<AttachmentRestrictionDto> result = new ArrayList<AttachmentRestrictionDto>(attachments.size());
                for (Attachment attachment : attachments) {
                    long attachmentId = attachment.getId();
                    result.add(new AttachmentRestrictionDto(attachmentId, getAttachmentRestrictionName(attachmentId), isAllowedToDownload(attachmentId), isAllowedToRestrict(attachmentId)));
                }
                return result;
            }
        }.getResponse();
    }
}
