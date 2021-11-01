/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021 Vladimir Orany.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.pierrot.core.impl;

import com.agorapulse.pierrot.core.*;
import com.agorapulse.pierrot.core.impl.client.GitHubHttpClient;
import com.agorapulse.pierrot.core.util.LoggerWithOptionalStacktrace;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;
import org.kohsuke.github.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.micronaut.core.annotation.TypeHint.AccessType.*;

@Singleton
@TypeHint(
    typeNames = {
        "org.kohsuke.github.GHApp",
        "org.kohsuke.github.GHAppCreateTokenBuilder",
        "org.kohsuke.github.GHAppInstallation",
        "org.kohsuke.github.GHAppInstallationToken",
        "org.kohsuke.github.GHAsset",
        "org.kohsuke.github.GHAuthorization",
        "org.kohsuke.github.GHAuthorization$App",
        "org.kohsuke.github.GHBlob",
        "org.kohsuke.github.GHBlobBuilder",
        "org.kohsuke.github.GHBranch",
        "org.kohsuke.github.GHBranch$Commit",
        "org.kohsuke.github.GHBranchProtection",
        "org.kohsuke.github.GHBranchProtection$EnforceAdmins",
        "org.kohsuke.github.GHBranchProtection$RequiredReviews",
        "org.kohsuke.github.GHBranchProtection$RequiredSignatures",
        "org.kohsuke.github.GHBranchProtection$RequiredStatusChecks",
        "org.kohsuke.github.GHBranchProtection$Restrictions",
        "org.kohsuke.github.GHBranchProtectionBuilder",
        "org.kohsuke.github.GHBranchProtectionBuilder$Restrictions",
        "org.kohsuke.github.GHBranchProtectionBuilder$StatusChecks",
        "org.kohsuke.github.GHCheckRun",
        "org.kohsuke.github.GHCheckRun$AnnotationLevel",
        "org.kohsuke.github.GHCheckRun$Conclusion",
        "org.kohsuke.github.GHCheckRun$Output",
        "org.kohsuke.github.GHCheckRun$Status",
        "org.kohsuke.github.GHCheckRunBuilder",
        "org.kohsuke.github.GHCheckRunBuilder$Action",
        "org.kohsuke.github.GHCheckRunBuilder$Annotation",
        "org.kohsuke.github.GHCheckRunBuilder$Image",
        "org.kohsuke.github.GHCheckRunBuilder$Output",
        "org.kohsuke.github.GHCheckRunsIterable",
        "org.kohsuke.github.GHCheckRunsPage",
        "org.kohsuke.github.GHCheckSuite",
        "org.kohsuke.github.GHCheckSuite$HeadCommit",
        "org.kohsuke.github.GHCommentAuthorAssociation",
        "org.kohsuke.github.GHCommit",
        "org.kohsuke.github.GHCommit$File",
        "org.kohsuke.github.GHCommit$GHAuthor",
        "org.kohsuke.github.GHCommit$Parent",
        "org.kohsuke.github.GHCommit$ShortInfo",
        "org.kohsuke.github.GHCommit$ShortInfo$Tree",
        "org.kohsuke.github.GHCommit$Stats",
        "org.kohsuke.github.GHCommit$User",
        "org.kohsuke.github.GHCommitBuilder",
        "org.kohsuke.github.GHCommitBuilder$UserInfo",
        "org.kohsuke.github.GHCommitComment",
        "org.kohsuke.github.GHCommitPointer",
        "org.kohsuke.github.GHCommitQueryBuilder",
        "org.kohsuke.github.GHCommitSearchBuilder",
        "org.kohsuke.github.GHCommitSearchBuilder$CommitSearchResult",
        "org.kohsuke.github.GHCommitSearchBuilder$Sort",
        "org.kohsuke.github.GHCommitState",
        "org.kohsuke.github.GHCommitStatus",
        "org.kohsuke.github.GHCompare",
        "org.kohsuke.github.GHCompare$Commit",
        "org.kohsuke.github.GHCompare$InnerCommit",
        "org.kohsuke.github.GHCompare$Status",
        "org.kohsuke.github.GHCompare$Tree",
        "org.kohsuke.github.GHCompare$User",
        "org.kohsuke.github.GHContent",
        "org.kohsuke.github.GHContentBuilder",
        "org.kohsuke.github.GHContentSearchBuilder",
        "org.kohsuke.github.GHContentSearchBuilder$ContentSearchResult",
        "org.kohsuke.github.GHContentUpdateResponse",
        "org.kohsuke.github.GHContentWithLicense",
        "org.kohsuke.github.GHCreateRepositoryBuilder",
        "org.kohsuke.github.GHDeployKey",
        "org.kohsuke.github.GHDeployment",
        "org.kohsuke.github.GHDeploymentBuilder",
        "org.kohsuke.github.GHDeploymentState",
        "org.kohsuke.github.GHDeploymentStatus",
        "org.kohsuke.github.GHDeploymentStatusBuilder",
        "org.kohsuke.github.GHDirection",
        "org.kohsuke.github.GHDiscussion",
        "org.kohsuke.github.GHDiscussion$Creator",
        "org.kohsuke.github.GHDiscussion$Setter",
        "org.kohsuke.github.GHDiscussion$Updater",
        "org.kohsuke.github.GHDiscussionBuilder",
        "org.kohsuke.github.GHEmail",
        "org.kohsuke.github.GHEvent",
        "org.kohsuke.github.GHEventInfo",
        "org.kohsuke.github.GHEventInfo$GHEventRepository",
        "org.kohsuke.github.GHEventPayload",
        "org.kohsuke.github.GHEventPayload$CheckRun",
        "org.kohsuke.github.GHEventPayload$CheckSuite",
        "org.kohsuke.github.GHEventPayload$CommitComment",
        "org.kohsuke.github.GHEventPayload$Create",
        "org.kohsuke.github.GHEventPayload$Delete",
        "org.kohsuke.github.GHEventPayload$Deployment",
        "org.kohsuke.github.GHEventPayload$DeploymentStatus",
        "org.kohsuke.github.GHEventPayload$Fork",
        "org.kohsuke.github.GHEventPayload$Installation",
        "org.kohsuke.github.GHEventPayload$InstallationRepositories",
        "org.kohsuke.github.GHEventPayload$Issue",
        "org.kohsuke.github.GHEventPayload$IssueComment",
        "org.kohsuke.github.GHEventPayload$Ping",
        "org.kohsuke.github.GHEventPayload$Public",
        "org.kohsuke.github.GHEventPayload$PullRequest",
        "org.kohsuke.github.GHEventPayload$PullRequestReview",
        "org.kohsuke.github.GHEventPayload$PullRequestReviewComment",
        "org.kohsuke.github.GHEventPayload$Push",
        "org.kohsuke.github.GHEventPayload$Push$PushCommit",
        "org.kohsuke.github.GHEventPayload$Push$Pusher",
        "org.kohsuke.github.GHEventPayload$Release",
        "org.kohsuke.github.GHEventPayload$Repository",
        "org.kohsuke.github.GHEventPayload$Status",
        "org.kohsuke.github.GHException",
        "org.kohsuke.github.GHFileNotFoundException",
        "org.kohsuke.github.GHGist",
        "org.kohsuke.github.GHGistBuilder",
        "org.kohsuke.github.GHGistFile",
        "org.kohsuke.github.GHGistUpdater",
        "org.kohsuke.github.GHHook",
        "org.kohsuke.github.GHHooks",
        "org.kohsuke.github.GHHooks$Context",
        "org.kohsuke.github.GHHooks$OrgContext",
        "org.kohsuke.github.GHHooks$RepoContext",
        "org.kohsuke.github.GHInvitation",
        "org.kohsuke.github.GHIOException",
        "org.kohsuke.github.GHIssue",
        "org.kohsuke.github.GHIssue$PullRequest",
        "org.kohsuke.github.GHIssueBuilder",
        "org.kohsuke.github.GHIssueComment",
        "org.kohsuke.github.GHIssueEvent",
        "org.kohsuke.github.GHIssueSearchBuilder",
        "org.kohsuke.github.GHIssueSearchBuilder$IssueSearchResult",
        "org.kohsuke.github.GHIssueSearchBuilder$Sort",
        "org.kohsuke.github.GHIssueState",
        "org.kohsuke.github.GHKey",
        "org.kohsuke.github.GHLabel",
        "org.kohsuke.github.GHLabel$Creator",
        "org.kohsuke.github.GHLabel$Setter",
        "org.kohsuke.github.GHLabel$Updater",
        "org.kohsuke.github.GHLabelBuilder",
        "org.kohsuke.github.GHLicense",
        "org.kohsuke.github.GHMarketplaceAccount",
        "org.kohsuke.github.GHMarketplaceAccountPlan",
        "org.kohsuke.github.GHMarketplaceAccountType",
        "org.kohsuke.github.GHMarketplaceListAccountBuilder",
        "org.kohsuke.github.GHMarketplaceListAccountBuilder$Sort",
        "org.kohsuke.github.GHMarketplacePendingChange",
        "org.kohsuke.github.GHMarketplacePlan",
        "org.kohsuke.github.GHMarketplacePriceModel",
        "org.kohsuke.github.GHMarketplacePurchase",
        "org.kohsuke.github.GHMarketplaceUserPurchase",
        "org.kohsuke.github.GHMembership",
        "org.kohsuke.github.GHMembership$Role",
        "org.kohsuke.github.GHMembership$State",
        "org.kohsuke.github.GHMeta",
        "org.kohsuke.github.GHMilestone",
        "org.kohsuke.github.GHMilestoneState",
        "org.kohsuke.github.GHMyself",
        "org.kohsuke.github.GHMyself$RepositoryListFilter",
        "org.kohsuke.github.GHNotificationStream",
        "org.kohsuke.github.GHObject",
        "org.kohsuke.github.GHOrganization",
        "org.kohsuke.github.GHOrganization$Permission",
        "org.kohsuke.github.GHOrganization$Role",
        "org.kohsuke.github.GHOrgHook",
        "org.kohsuke.github.GHOTPRequiredException",
        "org.kohsuke.github.GHPermission",
        "org.kohsuke.github.GHPermissionType",
        "org.kohsuke.github.GHPerson",
        "org.kohsuke.github.GHPersonSet",
        "org.kohsuke.github.GHProject",
        "org.kohsuke.github.GHProject$ProjectState",
        "org.kohsuke.github.GHProject$ProjectStateFilter",
        "org.kohsuke.github.GHProjectCard",
        "org.kohsuke.github.GHProjectColumn",
        "org.kohsuke.github.GHPullRequest",
        "org.kohsuke.github.GHPullRequest$MergeMethod",
        "org.kohsuke.github.GHPullRequestCommitDetail",
        "org.kohsuke.github.GHPullRequestCommitDetail$Authorship",
        "org.kohsuke.github.GHPullRequestCommitDetail$Commit",
        "org.kohsuke.github.GHPullRequestCommitDetail$CommitPointer",
        "org.kohsuke.github.GHPullRequestCommitDetail$Tree",
        "org.kohsuke.github.GHPullRequestFileDetail",
        "org.kohsuke.github.GHPullRequestQueryBuilder",
        "org.kohsuke.github.GHPullRequestQueryBuilder$Sort",
        "org.kohsuke.github.GHPullRequestReview",
        "org.kohsuke.github.GHPullRequestReviewBuilder",
        "org.kohsuke.github.GHPullRequestReviewBuilder$DraftReviewComment",
        "org.kohsuke.github.GHPullRequestReviewComment",
        "org.kohsuke.github.GHPullRequestReviewEvent",
        "org.kohsuke.github.GHPullRequestReviewState",
        "org.kohsuke.github.GHQueryBuilder",
        "org.kohsuke.github.GHRateLimit",
        "org.kohsuke.github.GHRateLimit$Record",
        "org.kohsuke.github.GHRateLimit$UnknownLimitRecord",
        "org.kohsuke.github.GHReaction",
        "org.kohsuke.github.GHRef",
        "org.kohsuke.github.GHRef$GHObject",
        "org.kohsuke.github.GHObject",
        "org.kohsuke.github.GHRelease",
        "org.kohsuke.github.GHReleaseBuilder",
        "org.kohsuke.github.GHReleaseUpdater",
        "org.kohsuke.github.GHRepoHook",
        "org.kohsuke.github.GHRepository",
        "org.kohsuke.github.GHRepository$Contributor",
        "org.kohsuke.github.GHRepository$ForkSort",
        "org.kohsuke.github.GHRepository$GHRepoPermission",
        "org.kohsuke.github.GHRepository$Topics",
        "org.kohsuke.github.GHRepositoryCloneTraffic",
        "org.kohsuke.github.GHRepositoryCloneTraffic$DailyInfo",
        "org.kohsuke.github.GHRepositorySearchBuilder",
        "org.kohsuke.github.GHRepositorySearchBuilder$RepositorySearchResult",
        "org.kohsuke.github.GHRepositorySearchBuilder$Sort",
        "org.kohsuke.github.GHRepositorySelection",
        "org.kohsuke.github.GHRepositoryStatistics",
        "org.kohsuke.github.GHRepositoryStatistics$CodeFrequency",
        "org.kohsuke.github.GHRepositoryStatistics$CommitActivity",
        "org.kohsuke.github.GHRepositoryStatistics$ContributorStats",
        "org.kohsuke.github.GHRepositoryStatistics$ContributorStats$Week",
        "org.kohsuke.github.GHRepositoryStatistics$Participation",
        "org.kohsuke.github.GHRepositoryStatistics$PunchCardItem",
        "org.kohsuke.github.GHRepositoryTraffic",
        "org.kohsuke.github.GHRepositoryTraffic$DailyInfo",
        "org.kohsuke.github.GHRepositoryViewTraffic",
        "org.kohsuke.github.GHRepositoryViewTraffic$DailyInfo",
        "org.kohsuke.github.GHRequestedAction",
        "org.kohsuke.github.GHSearchBuilder",
        "org.kohsuke.github.GHStargazer",
        "org.kohsuke.github.GHSubscription",
        "org.kohsuke.github.GHTag",
        "org.kohsuke.github.GHTagObject",
        "org.kohsuke.github.GHTargetType",
        "org.kohsuke.github.GHTeam",
        "org.kohsuke.github.GHTeam$Privacy",
        "org.kohsuke.github.GHTeam$Role",
        "org.kohsuke.github.GHTeamBuilder",
        "org.kohsuke.github.GHThread",
        "org.kohsuke.github.GHThread$Subject",
        "org.kohsuke.github.GHTree",
        "org.kohsuke.github.GHTreeBuilder",
        "org.kohsuke.github.GHTreeBuilder$TreeEntry",
        "org.kohsuke.github.GHTreeEntry",
        "org.kohsuke.github.GHUser",
        "org.kohsuke.github.GHUserSearchBuilder",
        "org.kohsuke.github.GHUserSearchBuilder$Sort",
        "org.kohsuke.github.GHUserSearchBuilder$UserSearchResult",
        "org.kohsuke.github.GHVerification",
        "org.kohsuke.github.GHVerification$Reason",
        "org.kohsuke.github.GHVerifiedKey",
    },
    accessType = {
        ALL_PUBLIC,
        ALL_DECLARED_CONSTRUCTORS,
        ALL_PUBLIC_CONSTRUCTORS,
        ALL_DECLARED_METHODS,
        ALL_DECLARED_FIELDS,
        ALL_PUBLIC_METHODS,
        ALL_PUBLIC_FIELDS
    }
)
public class DefaultGitHubService implements GitHubService {

    // the field is not static to prevent GraalVM FileAppender issues
    private static final Logger LOGGER = LoggerWithOptionalStacktrace.create(DefaultGitHubService.class);

    private static final String PR_URL_REPO_PREFIX = "https://api.github.com/repos/";
    private static final String PR_URL_REPO_SUFFIX = "/issues/";


    private final GitHubConfiguration configuration;
    private final GitHub client;
    private final GitHubHttpClient httpClient;

    private GHMyself myself;

    public DefaultGitHubService(GitHubConfiguration configuration, GitHub client, GitHubHttpClient httpClient) {
        this.configuration = configuration;
        this.client = client;
        this.httpClient = httpClient;
    }

    @Override
    public Stream<Content> searchContent(String query, boolean global) {
        return StreamSupport.stream(client.searchContent().q(addOrg(query, global)).list().spliterator(), false).map((GHContent content) ->
            new DefaultContent(content, content.getOwner(), getMyself(), configuration, httpClient)
        );
    }

    @Override
    public Optional<Repository> getRepository(String repositoryFullName) {
        try {
            return Optional.of(client.getRepository(repositoryFullName)).map((GHRepository repository) -> new DefaultRepository(repository, getMyself(), configuration, httpClient));
        } catch (IOException e) {
            LOGGER.error("Exception fetching repository " + repositoryFullName, e);
            return Optional.empty();
        }
    }

    @Override
    public Stream<? extends PullRequest> searchPullRequests(String query, boolean openOnly, boolean global) {
        if (openOnly) {
            query += " is:open";
        }
        return StreamSupport
            .stream(client.searchIssues().q(addOrg("is:pr " + query, global)).list().spliterator(), false)
            .map(issue -> {
                try {
                    String url = issue.getUrl().toString();
                    String repoFullName = url.substring(PR_URL_REPO_PREFIX.length(), url.lastIndexOf(PR_URL_REPO_SUFFIX));
                    GHRepository repository = client.getRepository(repoFullName);
                    GHPullRequest pullRequest = repository.getPullRequest(issue.getNumber());
                    return new DefaultPullRequest(pullRequest, repository, myself, configuration, httpClient);
                } catch (IOException e) {
                    LOGGER.error("Exception fetching pull request " + issue.getPullRequest().getUrl(),  e);
                    return null;
                }
            })
            .filter(Objects::nonNull);
    }

    @Override
    public Optional<Project> findOrCreateProject(String org, String project, String column) {
        try {
            return StreamSupport.stream(client.getOrganization(org).listProjects().spliterator(), false)
                .filter(p -> project.equals(p.getName()))
                .findFirst()
                .or(() -> {
                    try {
                        GHProject newProject = client.getOrganization(org).createProject(project, "Created by Pierrot");
                        newProject.createColumn(column);
                        LOGGER.info("New project created, you will need to add more column and set up automation yourself!");
                        LOGGER.info("    {}", newProject.getHtmlUrl());
                        return Optional.of(newProject);
                    } catch (IOException e) {
                        LOGGER.error("Exception creating project " + project + " in organization " + org, e);
                        return Optional.empty();
                    }
                })
                .map(DefaultProject::new);

        } catch (IOException e) {
            LOGGER.error("Exception fetching organization " + org, e);
            return Optional.empty();
        }
    }

    private GHUser getMyself() {
        if (myself != null) {
            return myself;
        }
        try {
            this.myself = client.getMyself();
            return myself;
        } catch (IOException e) {
            LOGGER.error("Exception fetching current user", e);
            return new GHUser();
        }
    }

    private String addOrg(String query, boolean global) {
        if (global) {
            return query;
        }

        if (query != null && query.contains("org:")) {
            return query;
        }

        if (StringUtils.isEmpty(configuration.getOrganization())) {
            LOGGER.warn("Organization is not set. You are searching the whole GitHub. Use GITHUB_ORGANIZATION environment variable or use 'org:myorg' in the search");
            return query;
        }

        return "org:" + configuration.getOrganization() + " " + query;
    }
}
