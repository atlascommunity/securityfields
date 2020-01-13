package ru.atlasteam.jira.plugins.security.attachments;

import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.issue.AttachFile;
import com.atlassian.jira.web.action.issue.util.BackwardCompatibleTemporaryAttachmentUtil;
import com.atlassian.jira.web.action.message.MessageResponder;
import com.atlassian.jira.web.action.message.PopUpMessageFactory;

import java.util.Date;

public class SecurityAttachRestrictedFileAction extends AttachFile {
    private final AttachmentRestrictionManager attachmentRestrictionManager;
    private final ChangeHistoryManager changeHistoryManager;

    private String attachmentRestriction;

    public SecurityAttachRestrictedFileAction(SubTaskManager subTaskManager, FieldScreenRendererFactory fieldScreenRendererFactory, FieldManager fieldManager, ProjectRoleManager projectRoleManager, CommentService commentService, AttachmentService attachmentService, IssueUpdater issueUpdater, BackwardCompatibleTemporaryAttachmentUtil temporaryAttachmentUtil, UserUtil userUtil, ChangeHistoryManager changeHistoryManager, MessageResponder responder, PopUpMessageFactory messageFactory, AttachmentRestrictionManager attachmentRestrictionManager) {
        super(subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService, attachmentService, issueUpdater, userUtil, temporaryAttachmentUtil, responder, messageFactory);
        this.attachmentRestrictionManager = attachmentRestrictionManager;
        this.changeHistoryManager = changeHistoryManager;
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception {
        long timeBeforeAttach = (new Date().getTime() / 1000) * 1000;
        String response = super.doExecute();
        long timeAfterAttach = (new Date().getTime() / 1000) * 1000;

        ApplicationUser user = getLoggedInUser();
        for (ChangeHistory changeHistory : changeHistoryManager.getChangeHistories(getIssueObject())) {
            if (changeHistory.getAuthorObject().equals(user))
                for (ChangeItemBean changeItemBean : changeHistory.getChangeItemBeans())
                    if (changeItemBean.getField().equals("Attachment") && changeItemBean.getCreated().getTime() >= timeBeforeAttach && changeItemBean.getCreated().getTime() <= timeAfterAttach)
                        attachmentRestrictionManager.updateAttachmentRestriction(Long.parseLong(changeItemBean.getTo()), attachmentRestriction);
        }

        return response;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setAttachmentRestriction(final String attachmentRestriction) {
        this.attachmentRestriction = attachmentRestriction;
    }
}
