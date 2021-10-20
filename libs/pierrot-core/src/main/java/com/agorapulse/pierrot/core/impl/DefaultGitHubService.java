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

import com.agorapulse.pierrot.core.Content;
import com.agorapulse.pierrot.core.GitHubConfiguration;
import com.agorapulse.pierrot.core.GitHubService;
import com.agorapulse.pierrot.core.Repository;
import io.micronaut.core.annotation.TypeHint;
import jakarta.inject.Singleton;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
        "org.kohsuke.github.GHBlob",
        "org.kohsuke.github.GHBlobBuilder",
        "org.kohsuke.github.GHBranch",
        "org.kohsuke.github.GHBranchProtection",
        "org.kohsuke.github.GHBranchProtectionBuilder",
        "org.kohsuke.github.GHCheckRun",
        "org.kohsuke.github.GHCheckRunBuilder",
        "org.kohsuke.github.GHCheckRunsIterable",
        "org.kohsuke.github.GHCheckRunsPage",
        "org.kohsuke.github.GHCheckSuite",
        "org.kohsuke.github.GHCommentAuthorAssociation",
        "org.kohsuke.github.GHCommit",
        "org.kohsuke.github.GHCommitBuilder",
        "org.kohsuke.github.GHCommitComment",
        "org.kohsuke.github.GHCommitPointer",
        "org.kohsuke.github.GHCommitQueryBuilder",
        "org.kohsuke.github.GHCommitSearchBuilder",
        "org.kohsuke.github.GHCommitState",
        "org.kohsuke.github.GHCommitStatus",
        "org.kohsuke.github.GHCompare",
        "org.kohsuke.github.GHContent",
        "org.kohsuke.github.GHContentBuilder",
        "org.kohsuke.github.GHContentSearchBuilder",
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
        "org.kohsuke.github.GHDiscussionBuilder",
        "org.kohsuke.github.GHEmail",
        "org.kohsuke.github.GHEvent",
        "org.kohsuke.github.GHEventInfo",
        "org.kohsuke.github.GHEventPayload",
        "org.kohsuke.github.GHException",
        "org.kohsuke.github.GHFileNotFoundException",
        "org.kohsuke.github.GHGist",
        "org.kohsuke.github.GHGistBuilder",
        "org.kohsuke.github.GHGistFile",
        "org.kohsuke.github.GHGistUpdater",
        "org.kohsuke.github.GHHook",
        "org.kohsuke.github.GHHooks",
        "org.kohsuke.github.GHInvitation",
        "org.kohsuke.github.GHIOException",
        "org.kohsuke.github.GHIssue",
        "org.kohsuke.github.GHIssueBuilder",
        "org.kohsuke.github.GHIssueComment",
        "org.kohsuke.github.GHIssueEvent",
        "org.kohsuke.github.GHIssueSearchBuilder",
        "org.kohsuke.github.GHIssueState",
        "org.kohsuke.github.GHKey",
        "org.kohsuke.github.GHLabel",
        "org.kohsuke.github.GHLabelBuilder",
        "org.kohsuke.github.GHLicense",
        "org.kohsuke.github.GHMarketplaceAccount",
        "org.kohsuke.github.GHMarketplaceAccountPlan",
        "org.kohsuke.github.GHMarketplaceAccountType",
        "org.kohsuke.github.GHMarketplaceListAccountBuilder",
        "org.kohsuke.github.GHMarketplacePendingChange",
        "org.kohsuke.github.GHMarketplacePlan",
        "org.kohsuke.github.GHMarketplacePriceModel",
        "org.kohsuke.github.GHMarketplacePurchase",
        "org.kohsuke.github.GHMarketplaceUserPurchase",
        "org.kohsuke.github.GHMembership",
        "org.kohsuke.github.GHMeta",
        "org.kohsuke.github.GHMilestone",
        "org.kohsuke.github.GHMilestoneState",
        "org.kohsuke.github.GHMyself",
        "org.kohsuke.github.GHNotificationStream",
        "org.kohsuke.github.GHObject",
        "org.kohsuke.github.GHOrganization",
        "org.kohsuke.github.GHOrgHook",
        "org.kohsuke.github.GHOTPRequiredException",
        "org.kohsuke.github.GHPermission",
        "org.kohsuke.github.GHPermissionType",
        "org.kohsuke.github.GHPerson",
        "org.kohsuke.github.GHPersonSet",
        "org.kohsuke.github.GHProject",
        "org.kohsuke.github.GHProjectCard",
        "org.kohsuke.github.GHProjectColumn",
        "org.kohsuke.github.GHPullRequest",
        "org.kohsuke.github.GHPullRequestCommitDetail",
        "org.kohsuke.github.GHPullRequestFileDetail",
        "org.kohsuke.github.GHPullRequestQueryBuilder",
        "org.kohsuke.github.GHPullRequestReview",
        "org.kohsuke.github.GHPullRequestReviewBuilder",
        "org.kohsuke.github.GHPullRequestReviewComment",
        "org.kohsuke.github.GHPullRequestReviewEvent",
        "org.kohsuke.github.GHPullRequestReviewState",
        "org.kohsuke.github.GHQueryBuilder",
        "org.kohsuke.github.GHRateLimit",
        "org.kohsuke.github.GHReaction",
        "org.kohsuke.github.GHRef",
        "org.kohsuke.github.GHRelease",
        "org.kohsuke.github.GHReleaseBuilder",
        "org.kohsuke.github.GHReleaseUpdater",
        "org.kohsuke.github.GHRepoHook",
        "org.kohsuke.github.GHRepository",
        "org.kohsuke.github.GHRepositoryCloneTraffic",
        "org.kohsuke.github.GHRepositorySearchBuilder",
        "org.kohsuke.github.GHRepositorySelection",
        "org.kohsuke.github.GHRepositoryStatistics",
        "org.kohsuke.github.GHRepositoryTraffic",
        "org.kohsuke.github.GHRepositoryViewTraffic",
        "org.kohsuke.github.GHRequestedAction",
        "org.kohsuke.github.GHSearchBuilder",
        "org.kohsuke.github.GHStargazer",
        "org.kohsuke.github.GHSubscription",
        "org.kohsuke.github.GHTag",
        "org.kohsuke.github.GHTagObject",
        "org.kohsuke.github.GHTargetType",
        "org.kohsuke.github.GHTeam",
        "org.kohsuke.github.GHTeamBuilder",
        "org.kohsuke.github.GHThread",
        "org.kohsuke.github.GHTree",
        "org.kohsuke.github.GHTreeBuilder",
        "org.kohsuke.github.GHTreeEntry",
        "org.kohsuke.github.GHUser",
        "org.kohsuke.github.GHUserSearchBuilder",
        "org.kohsuke.github.GHVerification",
        "org.kohsuke.github.GHVerifiedKey",
        "org.kohsuke.github.GitHub",
        "org.kohsuke.github.GitHubBuilder",
        "org.kohsuke.github.GitHubClient",
        "org.kohsuke.github.GitHubHttpUrlConnectionClient",
        "org.kohsuke.github.GitHubPageContentsIterable",
        "org.kohsuke.github.GitHubPageIterator",
        "org.kohsuke.github.GitHubRateLimitChecker",
        "org.kohsuke.github.GitHubRequest",
        "org.kohsuke.github.GitHubResponse",
    },
    accessType = {
        ALL_DECLARED_CONSTRUCTORS,
        ALL_DECLARED_METHODS,
        ALL_DECLARED_FIELDS,
    }
)
public class DefaultGitHubService implements GitHubService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGitHubService.class);

    private final GitHubConfiguration configuration;
    private final GitHub client;

    private GHMyself myself;

    public DefaultGitHubService(GitHubConfiguration configuration, GitHub client) {
        this.configuration = configuration;
        this.client = client;
    }

    @Override
    public Stream<Content> search(String query) {
        return StreamSupport.stream(client.searchContent().q(query).list().spliterator(), false).map((GHContent content) ->
            new DefaultContent(content, content.getOwner(), getMyself(), configuration)
        );
    }

    @Override
    public Optional<Repository> getRepository(String repositoryFullName) {
        try {
            return Optional.of(client.getRepository(repositoryFullName)).map((GHRepository repository) -> new DefaultRepository(repository, getMyself(), configuration));
        } catch (IOException e) {
            LOGGER.error("Exception fetching repository " + repositoryFullName, e);
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
            LOGGER.error("Exception fetching current user ", e);
            return new GHUser();
        }
    }
}
