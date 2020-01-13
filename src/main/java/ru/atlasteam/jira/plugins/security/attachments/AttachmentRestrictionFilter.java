package ru.atlasteam.jira.plugins.security.attachments;

import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttachmentRestrictionFilter implements Filter {
    private final static Pattern DOWNLOAD_ATTACHMENT_URL_REGEXP = Pattern.compile(".*/secure/(attachment|attachmentzip/unzip/[0-9]+)/([0-9]+)(\\[.*\\])?/.*");
    private final static Pattern DOWNLOAD_ALL_ATTACHMENTS_URL_REGEXP = Pattern.compile(".*/secure/attachmentzip/([0-9]+).zip");
    private final static Pattern DELETE_ATTACHMENT_URL_REGEXP = Pattern.compile(".*/secure/DeleteAttachment.jspa");

    private final AttachmentManager attachmentManager;
    private final AttachmentRestrictionManager attachmentRestrictionManager;
    private final IssueManager issueManager;

    private final static Log log = LogFactory.getLog(AttachmentRestrictionFilter.class);

    public AttachmentRestrictionFilter(AttachmentManager attachmentManager, AttachmentRestrictionManager attachmentRestrictionManager, IssueManager issueManager) {
        this.attachmentManager = attachmentManager;
        this.attachmentRestrictionManager = attachmentRestrictionManager;
        this.issueManager = issueManager;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            String requestUrl = URLDecoder.decode(request.getRequestURL().toString(), "UTF-8");
            Matcher matcher;

            matcher = DOWNLOAD_ATTACHMENT_URL_REGEXP.matcher(requestUrl);
            if (matcher.matches()) {
                if (!attachmentRestrictionManager.isAllowedToDownload(Long.parseLong(matcher.group(2))))
                    throw new SecurityException();
            }

            matcher = DOWNLOAD_ALL_ATTACHMENTS_URL_REGEXP.matcher(requestUrl);
            if (matcher.matches()) {
                Issue issue = issueManager.getIssueObject(Long.parseLong(matcher.group(1)));
                for (Attachment attachment : attachmentManager.getAttachments(issue))
                    if (!attachmentRestrictionManager.isAllowedToDownload(attachment.getId()))
                        throw new SecurityException();
            }

            matcher = DELETE_ATTACHMENT_URL_REGEXP.matcher(requestUrl);
            if (matcher.matches())
                if (!attachmentRestrictionManager.isAllowedToDownload(Long.parseLong(request.getParameter("deleteAttachmentId"))))
                    throw new SecurityException();

            filterChain.doFilter(servletRequest, servletResponse);
        } catch (SecurityException e) {
            response.sendRedirect(request.getContextPath() + "/secure/attachment/error/");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
    }
}
