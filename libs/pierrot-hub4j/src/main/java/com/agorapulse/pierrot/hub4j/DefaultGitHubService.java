/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2025 Vladimir Orany.
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
package com.agorapulse.pierrot.hub4j;

import com.agorapulse.pierrot.api.Content;
import com.agorapulse.pierrot.api.GitHubConfiguration;
import com.agorapulse.pierrot.api.GitHubService;
import com.agorapulse.pierrot.api.Project;
import com.agorapulse.pierrot.api.PullRequest;
import com.agorapulse.pierrot.api.Repository;
import com.agorapulse.pierrot.api.event.ProjectCreatedEvent;
import com.agorapulse.pierrot.api.util.LoggerWithOptionalStacktrace;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.client.HttpClient;
import jakarta.inject.Singleton;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHProject;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
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
        "org.kohsuke.github.GHAppFromManifest",
        "org.kohsuke.github.GHAppInstallation",
        "org.kohsuke.github.GHAppInstallation$GHAppInstallationRepositoryResult",
        "org.kohsuke.github.GHAppInstallationRequest",
        "org.kohsuke.github.GHAppInstallationToken",
        "org.kohsuke.github.GHAppInstallationsIterable",
        "org.kohsuke.github.GHAppInstallationsIterable$1",
        "org.kohsuke.github.GHAppInstallationsPage",
        "org.kohsuke.github.GHArtifact",
        "org.kohsuke.github.GHArtifactsIterable",
        "org.kohsuke.github.GHArtifactsIterable$1",
        "org.kohsuke.github.GHArtifactsPage",
        "org.kohsuke.github.GHAsset",
        "org.kohsuke.github.GHAuthenticatedAppInstallation",
        "org.kohsuke.github.GHAuthenticatedAppInstallation$GHAuthenticatedAppInstallationRepositoryResult",
        "org.kohsuke.github.GHAuthorization",
        "org.kohsuke.github.GHAuthorization$App",
        "org.kohsuke.github.GHAutolink",
        "org.kohsuke.github.GHAutolinkBuilder",
        "org.kohsuke.github.GHBlob",
        "org.kohsuke.github.GHBlobBuilder",
        "org.kohsuke.github.GHBranch",
        "org.kohsuke.github.GHBranch$1",
        "org.kohsuke.github.GHBranch$Commit",
        "org.kohsuke.github.GHBranchProtection",
        "org.kohsuke.github.GHBranchProtection$AllowDeletions",
        "org.kohsuke.github.GHBranchProtection$AllowForcePushes",
        "org.kohsuke.github.GHBranchProtection$AllowForkSyncing",
        "org.kohsuke.github.GHBranchProtection$BlockCreations",
        "org.kohsuke.github.GHBranchProtection$Check",
        "org.kohsuke.github.GHBranchProtection$EnforceAdmins",
        "org.kohsuke.github.GHBranchProtection$LockBranch",
        "org.kohsuke.github.GHBranchProtection$RequiredConversationResolution",
        "org.kohsuke.github.GHBranchProtection$RequiredLinearHistory",
        "org.kohsuke.github.GHBranchProtection$RequiredReviews",
        "org.kohsuke.github.GHBranchProtection$RequiredSignatures",
        "org.kohsuke.github.GHBranchProtection$RequiredStatusChecks",
        "org.kohsuke.github.GHBranchProtection$Restrictions",
        "org.kohsuke.github.GHBranchProtectionBuilder",
        "org.kohsuke.github.GHBranchProtectionBuilder$1",
        "org.kohsuke.github.GHBranchProtectionBuilder$Restrictions",
        "org.kohsuke.github.GHBranchProtectionBuilder$StatusChecks",
        "org.kohsuke.github.GHBranchSync",
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
        "org.kohsuke.github.GHCheckRunsIterable$1",
        "org.kohsuke.github.GHCheckRunsPage",
        "org.kohsuke.github.GHCheckSuite",
        "org.kohsuke.github.GHCheckSuite$HeadCommit",
        "org.kohsuke.github.GHCodeownersError",
        "org.kohsuke.github.GHCommentAuthorAssociation",
        "org.kohsuke.github.GHCommit",
        "org.kohsuke.github.GHCommit$1",
        "org.kohsuke.github.GHCommit$File",
        "org.kohsuke.github.GHCommit$GHAuthor",
        "org.kohsuke.github.GHCommit$Parent",
        "org.kohsuke.github.GHCommit$ShortInfo",
        "org.kohsuke.github.GHCommit$Stats",
        "org.kohsuke.github.GHCommit$User",
        "org.kohsuke.github.GHCommitBuilder",
        "org.kohsuke.github.GHCommitBuilder$1",
        "org.kohsuke.github.GHCommitBuilder$UserInfo",
        "org.kohsuke.github.GHCommitComment",
        "org.kohsuke.github.GHCommitFileIterable",
        "org.kohsuke.github.GHCommitFileIterable$1",
        "org.kohsuke.github.GHCommitFilesPage",
        "org.kohsuke.github.GHCommitPointer",
        "org.kohsuke.github.GHCommitQueryBuilder",
        "org.kohsuke.github.GHCommitSearchBuilder",
        "org.kohsuke.github.GHCommitSearchBuilder$CommitSearchResult",
        "org.kohsuke.github.GHCommitSearchBuilder$Sort",
        "org.kohsuke.github.GHCommitState",
        "org.kohsuke.github.GHCommitStatus",
        "org.kohsuke.github.GHCompare",
        "org.kohsuke.github.GHCompare$1",
        "org.kohsuke.github.GHCompare$Commit",
        "org.kohsuke.github.GHCompare$GHCompareCommitsIterable",
        "org.kohsuke.github.GHCompare$GHCompareCommitsIterable$1",
        "org.kohsuke.github.GHCompare$InnerCommit",
        "org.kohsuke.github.GHCompare$Status",
        "org.kohsuke.github.GHCompare$Tree",
        "org.kohsuke.github.GHCompare$User",
        "org.kohsuke.github.GHContent",
        "org.kohsuke.github.GHContentBuilder",
        "org.kohsuke.github.GHContentSearchBuilder",
        "org.kohsuke.github.GHContentSearchBuilder$ContentSearchResult",
        "org.kohsuke.github.GHContentSearchBuilder$Sort",
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
        "org.kohsuke.github.GHDiscussion$1",
        "org.kohsuke.github.GHDiscussion$Creator",
        "org.kohsuke.github.GHDiscussion$Setter",
        "org.kohsuke.github.GHDiscussion$Updater",
        "org.kohsuke.github.GHDiscussionBuilder",
        "org.kohsuke.github.GHEmail",
        "org.kohsuke.github.GHEnterpriseManagedUsersException",
        "org.kohsuke.github.GHError",
        "org.kohsuke.github.GHEvent",
        "org.kohsuke.github.GHEventInfo",
        "org.kohsuke.github.GHEventInfo$GHEventRepository",
        "org.kohsuke.github.GHEventPayload",
        "org.kohsuke.github.GHEventPayload$CheckRun",
        "org.kohsuke.github.GHEventPayload$CheckSuite",
        "org.kohsuke.github.GHEventPayload$CommentChanges",
        "org.kohsuke.github.GHEventPayload$CommentChanges$GHFrom",
        "org.kohsuke.github.GHEventPayload$CommitComment",
        "org.kohsuke.github.GHEventPayload$Create",
        "org.kohsuke.github.GHEventPayload$Delete",
        "org.kohsuke.github.GHEventPayload$Deployment",
        "org.kohsuke.github.GHEventPayload$DeploymentStatus",
        "org.kohsuke.github.GHEventPayload$Discussion",
        "org.kohsuke.github.GHEventPayload$DiscussionComment",
        "org.kohsuke.github.GHEventPayload$Fork",
        "org.kohsuke.github.GHEventPayload$Installation",
        "org.kohsuke.github.GHEventPayload$Installation$Repository",
        "org.kohsuke.github.GHEventPayload$InstallationRepositories",
        "org.kohsuke.github.GHEventPayload$Issue",
        "org.kohsuke.github.GHEventPayload$IssueComment",
        "org.kohsuke.github.GHEventPayload$Label",
        "org.kohsuke.github.GHEventPayload$Member",
        "org.kohsuke.github.GHEventPayload$Membership",
        "org.kohsuke.github.GHEventPayload$Ping",
        "org.kohsuke.github.GHEventPayload$ProjectsV2Item",
        "org.kohsuke.github.GHEventPayload$Public",
        "org.kohsuke.github.GHEventPayload$PullRequest",
        "org.kohsuke.github.GHEventPayload$PullRequestReview",
        "org.kohsuke.github.GHEventPayload$PullRequestReviewComment",
        "org.kohsuke.github.GHEventPayload$Push",
        "org.kohsuke.github.GHEventPayload$Push$PushCommit",
        "org.kohsuke.github.GHEventPayload$Push$Pusher",
        "org.kohsuke.github.GHEventPayload$Release",
        "org.kohsuke.github.GHEventPayload$Repository",
        "org.kohsuke.github.GHEventPayload$Star",
        "org.kohsuke.github.GHEventPayload$Status",
        "org.kohsuke.github.GHEventPayload$Team",
        "org.kohsuke.github.GHEventPayload$TeamAdd",
        "org.kohsuke.github.GHEventPayload$WorkflowDispatch",
        "org.kohsuke.github.GHEventPayload$WorkflowJob",
        "org.kohsuke.github.GHEventPayload$WorkflowRun",
        "org.kohsuke.github.GHException",
        "org.kohsuke.github.GHExternalGroup",
        "org.kohsuke.github.GHExternalGroup$GHLinkedExternalMember",
        "org.kohsuke.github.GHExternalGroup$GHLinkedTeam",
        "org.kohsuke.github.GHExternalGroupIterable",
        "org.kohsuke.github.GHExternalGroupIterable$1",
        "org.kohsuke.github.GHExternalGroupPage",
        "org.kohsuke.github.GHFileNotFoundException",
        "org.kohsuke.github.GHFork",
        "org.kohsuke.github.GHGist",
        "org.kohsuke.github.GHGistBuilder",
        "org.kohsuke.github.GHGistFile",
        "org.kohsuke.github.GHGistUpdater",
        "org.kohsuke.github.GHHook",
        "org.kohsuke.github.GHHooks",
        "org.kohsuke.github.GHHooks$1",
        "org.kohsuke.github.GHHooks$Context",
        "org.kohsuke.github.GHHooks$OrgContext",
        "org.kohsuke.github.GHHooks$RepoContext",
        "org.kohsuke.github.GHIOException",
        "org.kohsuke.github.GHInvitation",
        "org.kohsuke.github.GHIssue",
        "org.kohsuke.github.GHIssue$PullRequest",
        "org.kohsuke.github.GHIssueBuilder",
        "org.kohsuke.github.GHIssueChanges",
        "org.kohsuke.github.GHIssueChanges$GHFrom",
        "org.kohsuke.github.GHIssueComment",
        "org.kohsuke.github.GHIssueCommentQueryBuilder",
        "org.kohsuke.github.GHIssueEvent",
        "org.kohsuke.github.GHIssueQueryBuilder",
        "org.kohsuke.github.GHIssueQueryBuilder$ForRepository",
        "org.kohsuke.github.GHIssueQueryBuilder$Sort",
        "org.kohsuke.github.GHIssueRename",
        "org.kohsuke.github.GHIssueSearchBuilder",
        "org.kohsuke.github.GHIssueSearchBuilder$IssueSearchResult",
        "org.kohsuke.github.GHIssueSearchBuilder$Sort",
        "org.kohsuke.github.GHIssueState",
        "org.kohsuke.github.GHIssueStateReason",
        "org.kohsuke.github.GHKey",
        "org.kohsuke.github.GHLabel",
        "org.kohsuke.github.GHLabel$1",
        "org.kohsuke.github.GHLabel$Creator",
        "org.kohsuke.github.GHLabel$Setter",
        "org.kohsuke.github.GHLabel$Updater",
        "org.kohsuke.github.GHLabelBuilder",
        "org.kohsuke.github.GHLabelChanges",
        "org.kohsuke.github.GHLabelChanges$GHFrom",
        "org.kohsuke.github.GHLicense",
        "org.kohsuke.github.GHMarketplaceAccount",
        "org.kohsuke.github.GHMarketplaceAccountPlan",
        "org.kohsuke.github.GHMarketplaceAccountType",
        "org.kohsuke.github.GHMarketplaceListAccountBuilder",
        "org.kohsuke.github.GHMarketplaceListAccountBuilder$Sort",
        "org.kohsuke.github.GHMarketplacePendingChange",
        "org.kohsuke.github.GHMarketplacePlan",
        "org.kohsuke.github.GHMarketplacePlanForAccountBuilder",
        "org.kohsuke.github.GHMarketplacePriceModel",
        "org.kohsuke.github.GHMarketplacePurchase",
        "org.kohsuke.github.GHMarketplaceUserPurchase",
        "org.kohsuke.github.GHMemberChanges",
        "org.kohsuke.github.GHMemberChanges$FromRoleName",
        "org.kohsuke.github.GHMemberChanges$FromToPermission",
        "org.kohsuke.github.GHMembership",
        "org.kohsuke.github.GHMembership$Role",
        "org.kohsuke.github.GHMembership$State",
        "org.kohsuke.github.GHMeta",
        "org.kohsuke.github.GHMilestone",
        "org.kohsuke.github.GHMilestoneState",
        "org.kohsuke.github.GHMyself",
        "org.kohsuke.github.GHMyself$RepositoryListFilter",
        "org.kohsuke.github.GHNotExternallyManagedEnterpriseException",
        "org.kohsuke.github.GHNotificationStream",
        "org.kohsuke.github.GHNotificationStream$1",
        "org.kohsuke.github.GHOTPRequiredException",
        "org.kohsuke.github.GHObject",
        "org.kohsuke.github.GHObject$1",
        "org.kohsuke.github.GHObject$2",
        "org.kohsuke.github.GHOrgHook",
        "org.kohsuke.github.GHOrganization",
        "org.kohsuke.github.GHOrganization$Permission",
        "org.kohsuke.github.GHOrganization$RepositoryRole",
        "org.kohsuke.github.GHOrganization$Role",
        "org.kohsuke.github.GHPermission",
        "org.kohsuke.github.GHPermissionType",
        "org.kohsuke.github.GHPerson",
        "org.kohsuke.github.GHPerson$1",
        "org.kohsuke.github.GHPersonSet",
        "org.kohsuke.github.GHProject",
        "org.kohsuke.github.GHProject$ProjectState",
        "org.kohsuke.github.GHProject$ProjectStateFilter",
        "org.kohsuke.github.GHProjectCard",
        "org.kohsuke.github.GHProjectColumn",
        "org.kohsuke.github.GHProjectsV2Item",
        "org.kohsuke.github.GHProjectsV2Item$ContentType",
        "org.kohsuke.github.GHProjectsV2ItemChanges",
        "org.kohsuke.github.GHProjectsV2ItemChanges$FieldType",
        "org.kohsuke.github.GHProjectsV2ItemChanges$FieldValue",
        "org.kohsuke.github.GHProjectsV2ItemChanges$FromTo",
        "org.kohsuke.github.GHProjectsV2ItemChanges$FromToDate",
        "org.kohsuke.github.GHPullRequest",
        "org.kohsuke.github.GHPullRequest$AutoMerge",
        "org.kohsuke.github.GHPullRequest$MergeMethod",
        "org.kohsuke.github.GHPullRequestChanges",
        "org.kohsuke.github.GHPullRequestChanges$GHCommitPointer",
        "org.kohsuke.github.GHPullRequestChanges$GHFrom",
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
        "org.kohsuke.github.GHPullRequestReviewBuilder$MultilineDraftReviewComment",
        "org.kohsuke.github.GHPullRequestReviewBuilder$ReviewComment",
        "org.kohsuke.github.GHPullRequestReviewBuilder$SingleLineDraftReviewComment",
        "org.kohsuke.github.GHPullRequestReviewComment",
        "org.kohsuke.github.GHPullRequestReviewComment$Side",
        "org.kohsuke.github.GHPullRequestReviewCommentBuilder",
        "org.kohsuke.github.GHPullRequestReviewCommentReactions",
        "org.kohsuke.github.GHPullRequestReviewEvent",
        "org.kohsuke.github.GHPullRequestReviewEvent$1",
        "org.kohsuke.github.GHPullRequestReviewState",
        "org.kohsuke.github.GHPullRequestReviewState$1",
        "org.kohsuke.github.GHPullRequestSearchBuilder",
        "org.kohsuke.github.GHPullRequestSearchBuilder$PullRequestSearchResult",
        "org.kohsuke.github.GHPullRequestSearchBuilder$Sort",
        "org.kohsuke.github.GHQueryBuilder",
        "org.kohsuke.github.GHRateLimit",
        "org.kohsuke.github.GHRateLimit$Record",
        "org.kohsuke.github.GHRateLimit$UnknownLimitRecord",
        "org.kohsuke.github.GHReaction",
        "org.kohsuke.github.GHRef",
        "org.kohsuke.github.GHRef$GHObject",
        "org.kohsuke.github.GHRelease",
        "org.kohsuke.github.GHReleaseBuilder",
        "org.kohsuke.github.GHReleaseBuilder$MakeLatest",
        "org.kohsuke.github.GHReleaseUpdater",
        "org.kohsuke.github.GHRepoHook",
        "org.kohsuke.github.GHRepository",
        "org.kohsuke.github.GHRepository$1",
        "org.kohsuke.github.GHRepository$CollaboratorAffiliation",
        "org.kohsuke.github.GHRepository$Contributor",
        "org.kohsuke.github.GHRepository$ForkSort",
        "org.kohsuke.github.GHRepository$GHCodeownersErrors",
        "org.kohsuke.github.GHRepository$GHRepoPermission",
        "org.kohsuke.github.GHRepository$Setter",
        "org.kohsuke.github.GHRepository$Topics",
        "org.kohsuke.github.GHRepository$Updater",
        "org.kohsuke.github.GHRepository$Visibility",
        "org.kohsuke.github.GHRepositoryBuilder",
        "org.kohsuke.github.GHRepositoryChanges",
        "org.kohsuke.github.GHRepositoryChanges$FromName",
        "org.kohsuke.github.GHRepositoryChanges$FromOwner",
        "org.kohsuke.github.GHRepositoryChanges$FromRepository",
        "org.kohsuke.github.GHRepositoryChanges$Owner",
        "org.kohsuke.github.GHRepositoryCloneTraffic",
        "org.kohsuke.github.GHRepositoryCloneTraffic$DailyInfo",
        "org.kohsuke.github.GHRepositoryDiscussion",
        "org.kohsuke.github.GHRepositoryDiscussion$Category",
        "org.kohsuke.github.GHRepositoryDiscussion$State",
        "org.kohsuke.github.GHRepositoryDiscussionComment",
        "org.kohsuke.github.GHRepositoryForkBuilder",
        "org.kohsuke.github.GHRepositoryPublicKey",
        "org.kohsuke.github.GHRepositoryRule",
        "org.kohsuke.github.GHRepositoryRule$AlertsThreshold",
        "org.kohsuke.github.GHRepositoryRule$BooleanParameter",
        "org.kohsuke.github.GHRepositoryRule$BooleanParameter$1",
        "org.kohsuke.github.GHRepositoryRule$CodeScanningTool",
        "org.kohsuke.github.GHRepositoryRule$IntegerParameter",
        "org.kohsuke.github.GHRepositoryRule$IntegerParameter$1",
        "org.kohsuke.github.GHRepositoryRule$ListParameter",
        "org.kohsuke.github.GHRepositoryRule$Operator",
        "org.kohsuke.github.GHRepositoryRule$Parameter",
        "org.kohsuke.github.GHRepositoryRule$Parameters",
        "org.kohsuke.github.GHRepositoryRule$Parameters$1",
        "org.kohsuke.github.GHRepositoryRule$Parameters$1$1",
        "org.kohsuke.github.GHRepositoryRule$Parameters$2",
        "org.kohsuke.github.GHRepositoryRule$Parameters$2$1",
        "org.kohsuke.github.GHRepositoryRule$Parameters$3",
        "org.kohsuke.github.GHRepositoryRule$Parameters$3$1",
        "org.kohsuke.github.GHRepositoryRule$Parameters$4",
        "org.kohsuke.github.GHRepositoryRule$Parameters$4$1",
        "org.kohsuke.github.GHRepositoryRule$Parameters$5",
        "org.kohsuke.github.GHRepositoryRule$Parameters$5$1",
        "org.kohsuke.github.GHRepositoryRule$RulesetSourceType",
        "org.kohsuke.github.GHRepositoryRule$SecurityAlertsThreshold",
        "org.kohsuke.github.GHRepositoryRule$StatusCheckConfiguration",
        "org.kohsuke.github.GHRepositoryRule$StringParameter",
        "org.kohsuke.github.GHRepositoryRule$StringParameter$1",
        "org.kohsuke.github.GHRepositoryRule$Type",
        "org.kohsuke.github.GHRepositoryRule$WorkflowFileReference",
        "org.kohsuke.github.GHRepositorySearchBuilder",
        "org.kohsuke.github.GHRepositorySearchBuilder$Fork",
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
        "org.kohsuke.github.GHRepositoryTrafficReferralBase",
        "org.kohsuke.github.GHRepositoryTrafficTopReferralPath",
        "org.kohsuke.github.GHRepositoryTrafficTopReferralSources",
        "org.kohsuke.github.GHRepositoryVariable",
        "org.kohsuke.github.GHRepositoryVariable$1",
        "org.kohsuke.github.GHRepositoryVariable$Creator",
        "org.kohsuke.github.GHRepositoryVariable$Setter",
        "org.kohsuke.github.GHRepositoryVariableBuilder",
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
        "org.kohsuke.github.GHTeamCannotBeExternallyManagedException",
        "org.kohsuke.github.GHTeamChanges",
        "org.kohsuke.github.GHTeamChanges$FromPrivacy",
        "org.kohsuke.github.GHTeamChanges$FromRepository",
        "org.kohsuke.github.GHTeamChanges$FromRepositoryPermissions",
        "org.kohsuke.github.GHTeamChanges$FromString",
        "org.kohsuke.github.GHThread",
        "org.kohsuke.github.GHThread$Subject",
        "org.kohsuke.github.GHTree",
        "org.kohsuke.github.GHTreeBuilder",
        "org.kohsuke.github.GHTreeBuilder$1",
        "org.kohsuke.github.GHTreeBuilder$DeleteTreeEntry",
        "org.kohsuke.github.GHTreeBuilder$TreeEntry",
        "org.kohsuke.github.GHTreeEntry",
        "org.kohsuke.github.GHUser",
        "org.kohsuke.github.GHUserSearchBuilder",
        "org.kohsuke.github.GHUserSearchBuilder$Sort",
        "org.kohsuke.github.GHUserSearchBuilder$UserSearchResult",
        "org.kohsuke.github.GHVerification",
        "org.kohsuke.github.GHVerification$Reason",
        "org.kohsuke.github.GHVerifiedKey",
        "org.kohsuke.github.GHWorkflow",
        "org.kohsuke.github.GHWorkflowJob",
        "org.kohsuke.github.GHWorkflowJob$Step",
        "org.kohsuke.github.GHWorkflowJobQueryBuilder",
        "org.kohsuke.github.GHWorkflowJobsIterable",
        "org.kohsuke.github.GHWorkflowJobsIterable$1",
        "org.kohsuke.github.GHWorkflowJobsPage",
        "org.kohsuke.github.GHWorkflowRun",
        "org.kohsuke.github.GHWorkflowRun$Conclusion",
        "org.kohsuke.github.GHWorkflowRun$HeadCommit",
        "org.kohsuke.github.GHWorkflowRun$Status",
        "org.kohsuke.github.GHWorkflowRunQueryBuilder",
        "org.kohsuke.github.GHWorkflowRunsIterable",
        "org.kohsuke.github.GHWorkflowRunsIterable$1",
        "org.kohsuke.github.GHWorkflowRunsPage",
        "org.kohsuke.github.GHWorkflowsIterable",
        "org.kohsuke.github.GHWorkflowsIterable$1",
        "org.kohsuke.github.GHWorkflowsPage",
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
    private final HttpClient httpClient;
    private final ApplicationEventPublisher publisher;

    private GHMyself myself;

    public DefaultGitHubService(GitHubConfiguration configuration, GitHub client, HttpClient httpClient, ApplicationEventPublisher publisher) {
        this.configuration = configuration;
        this.client = client;
        this.httpClient = httpClient;
        this.publisher = publisher;
    }

    @Override
    public Stream<Content> searchContent(String query, boolean global) {
        return StreamSupport.stream(client.searchContent().q(addOrg(query, global)).list().spliterator(), false).map((GHContent content) ->
            new DefaultContent(content, content.getOwner(), getMyself(), configuration, httpClient, publisher)
        );
    }

    @Override
    public Optional<Repository> getRepository(String repositoryFullName) {
        try {
            return Optional.of(client.getRepository(repositoryFullName)).map((GHRepository repository) -> new DefaultRepository(repository, getMyself(), configuration, httpClient, publisher));
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
                    return new DefaultPullRequest(pullRequest, repository, myself, configuration, httpClient, publisher);
                } catch (IOException e) {
                    LOGGER.error("Exception fetching pull request " + issue.getPullRequest().getUrl(),  e);
                    return null;
                }
            })
            .filter(Objects::nonNull);
    }

    @Override
    public Optional<Project> findProject(String org, String project) {
        try {
            return StreamSupport.stream(client.getOrganization(org).listProjects().spliterator(), false)
                .filter(p -> project.equals(p.getName()))
                .findFirst()
                .map(p -> new DefaultProject(p, publisher));
        } catch (IOException e) {
            LOGGER.error("Exception fetching organization " + org, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Project> findOrCreateProject(String org, String project, String column) {
        return findProject(org, project)
            .or(() -> {
                try {
                    GHProject newProject = client.getOrganization(org).createProject(project, "Created by Pierrot");
                    for (String newColumn : configuration.getProjectColumns()) {
                        newProject.createColumn(newColumn);
                    }

                    if (!configuration.getProjectColumns().contains(column)) {
                        newProject.createColumn(column);
                    }

                    LOGGER.info("New project created, you will need set up column automation yourself!");
                    LOGGER.info("    {}", newProject.getHtmlUrl());
                    DefaultProject projectWrapper = new DefaultProject(newProject, publisher);
                    publisher.publishEvent(new ProjectCreatedEvent(projectWrapper));
                    return Optional.of(projectWrapper);
                } catch (IOException e) {
                    LOGGER.error("Exception creating project " + project + " in organization " + org, e);
                    return Optional.empty();
                }
            });
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
            LOGGER.warn("Organization is not set. You are searching the whole GitHub. Use PIERROT_ORGANIZATION environment variable or use 'org:myorg' in the search");
            return query;
        }

        return "org:" + configuration.getOrganization() + " " + query;
    }
}
