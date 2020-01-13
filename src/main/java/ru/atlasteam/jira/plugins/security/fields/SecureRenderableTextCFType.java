package ru.atlasteam.jira.plugins.security.fields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.RenderableTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class SecureRenderableTextCFType extends RenderableTextCFType {
    private final SecuritySchemeService securitySchemeService;

    protected SecureRenderableTextCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager, JiraAuthenticationContext jiraAuthenticationContext, SecuritySchemeService securitySchemeService, TextFieldCharacterLengthValidator textFieldCharacterLengthValidator) {
        super(customFieldValuePersister, genericConfigManager, textFieldCharacterLengthValidator, jiraAuthenticationContext);
        this.securitySchemeService = securitySchemeService;
    }

    @Override
    public String getValueFromIssue(CustomField field, Issue issue) {
        if (securitySchemeService.isAccessibleForLoggedInUser(field, issue))
            return super.getValueFromIssue(field, issue);
        return null;
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> result = super.getVelocityParameters(issue, field, fieldLayoutItem);
        if (securitySchemeService.isAccessibleForLoggedInUser(field, issue))
            result.put("viewTemplate", "templates/plugins/fields/edit/edit-maxlengthtext.vm");
        return result;
    }

    @Override
    public String getChangelogValue(CustomField field, String value) {
        return null;
    }

    @Nonnull
    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new SecuritySchemeConfigItem());
        return configurationItemTypes;
    }
}
