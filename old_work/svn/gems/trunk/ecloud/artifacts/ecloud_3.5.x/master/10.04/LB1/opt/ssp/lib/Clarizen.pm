package Clarizen;

use strict;
use warnings;
use JSON;
use LWP::UserAgent;

sub new($) {
    my $class = shift;
    my $self = {};
    $self->{_ua} = LWP::UserAgent->new();
    bless $self, $class;
    return $self;
}

sub _getJSON($$) {
    my $self = shift;
    my $uri = $self->url(shift);
    my $req = HTTP::Request->new('GET', $uri);
    if($self->{_USID}) {
        $req->header('Authorization' => $self->{_USID});
    }
    my $resp = $self->{_ua}->request($req);
    die "GET: $uri failed " . $resp->code() unless $resp->is_success();
    return JSON::decode_json($resp->decoded_content());
}

sub _postJSON($$$) {
    my $self = shift;
    my $uri = $self->url(shift);
    my $json = JSON::encode_json(shift);
    my $req = HTTP::Request->new('POST', $uri);
    $req->header('Content-Type' => 'application/json');
    if($self->{_USID}) {
        $req->header('Authorization' => $self->{_USID});
    }
    $req->content($json);
    my $resp = $self->{_ua}->request($req);
    die "POST: $uri, $json failed " . $resp->code() unless $resp->is_success();
    return JSON::decode_json($resp->decoded_content());
}

sub url($$) {
    my $self = shift;
    my $path = shift;
    return $path unless $self->{_baseURL};
    return $self->{_baseURL} . $path;
}

sub login($$$) {
    my $self = shift;
    my $user = shift;
    my $pass = shift;
    my $creds = { UserName => $user, Password => $pass };
    my $content = $self->_postJSON("https://api2.clarizen.com/v2.0/services/authentication/getServerDefinition", $creds);
    $self->{_baseURL} = $content->{serverLocation};
    die "No server location specified" unless $self->{_baseURL};

    $content = $self->_postJSON('/authentication/login', $creds);
    die "No session id defined" unless $content->{sessionId};
    $self->{_USID} = "Session $content->{sessionId}";
    return;
}

sub getProjects($$;$$) {
    my $self = shift;
    my $fields = shift;
    my $constraints = shift;
    my $paging = shift;

    my $body =  {
        typeName => "Project",
        fields => $fields,
    };
    if($constraints) {
        $body->{where} = $constraints;
    }
    if($paging) {
        $body->{paging} = $paging;
    }
    my $content = $self->_postJSON('/data/entityQuery', $body);
    my $retVal;
    for my $proj (@{$content->{entities}}) {
        push @$retVal, Clarizen::Project->new($proj);
    }
    if(exists $content->{paging} && $content->{paging}->{hasMore}) {
        push @$retVal, @{$self->getProjects($fields, $constraints, $content->{paging})};
    }
    return $retVal;
}

package Clarizen::Project;

use strict;
use warnings;

my $clarizenFields = [ 'CreatedBy', 'CreatedOn', 'LastUpdatedBy', 'LastUpdatedOn', 'Name', 'Description', 'ExternalID', 'State', 'Phase', 'StartDate', 'DueDate', 'Duration', 'ActualStartDate',
    'ActualEndDate', 'TrackStatus', 'Conflicts', 'OnCriticalPath', 'DurationManuallySet', 'TrackStatusManuallySet', 'EarliestStartDate', 'LatestStartDate', 'EarliestEndDate', 'LatestEndDate',
    'ExpectedProgress', 'SchedulingType', 'ImportedFrom', 'Importance', 'Priority', 'PercentCompleted', 'Manager', 'ChargedTypeManuallySet', 'ChildrenCount', 'SuccessorsCount', 'PredecessorsCount',
    'AllResourcesCount', 'AttachmentsCount', 'PostsCount', 'NotesCount', 'Reportable', 'ReportableManuallySet', 'Billable', 'ChildShortcutCount', 'Project', 'WorkPolicy', 'CommitLevel', 'ReportingStartDate',
    'SYSID', 'Work', 'ActualEffort', 'RemainingEffort', 'WorkManuallySet', 'RemainingEffortManuallySet', 'WorkVariance', 'ActualDuration', 'StartDateVariance', 'ActualCost', 'DueDateVariance', 'PlannedBudget',
    'DurationVariance', 'ActualCostManuallySet', 'PlannedBudgetManuallySet', 'TimeTrackingEffort', 'TimeTrackingCost', 'FixedCost', 'FixedPrice', 'PercentInvested', 'CostVariance', 'TimeTrackingBilling',
    'EarnedValue', 'PlannedRevenue', 'CPI', 'ActualRevenue', 'SPI', 'PlannedRevenueManuallySet', 'ActualRevenueManuallySet', 'Profitability', 'PercentProfitability', 'PlannedExpenses', 'DirectPlannedExpenses',
    'ActualExpenses', 'DirectActualExpenses', 'ProjectedExpenses', 'DirectProjectedExpenses', 'PlannedBilledExpenses', 'DirectPlannedBilledExpenses', 'ActualBilledExpenses', 'DirectActualBilledExpenses',
    'ProjectedBilledExpenses', 'DirectProjectedBilledExpenses', 'RevenueCurrencyType', 'CostCurrencyType', 'Pending', 'IssuesCount', 'LastUpdatedBySystemOn', 'AllowReportingOnSubItems', 'CommittedDate',
    'ResourcesCount', 'EmailsCount', 'BudgetedHours', 'CostBalance', 'BudgetStatus', 'RevenueBalance', 'ActualEffortUpdatedFromTimesheets', 'ParentProject', 'SfExternalId', 'SfExternalName', 'InternalId',
    'OrderID', 'SKU', 'BaselineStartDate', 'BaselineStartDateVariance', 'BaselineDueDate', 'BaselineDueDateVariance', 'BaselineDuration', 'BaselineDurationVariance', 'BaselineWork', 'BaselineWorkVariance',
    'BaselineCost', 'BaselineCostsVariance', 'BaselineRevenue', 'BaselineRevenueVariance', 'InternalStatus', 'Deliverable', 'DeliverableType', 'Executable', 'Parent', 'PlannedAmount', 'ChargedAmount', 'TCPI',
    'TotalEstimatedCost', 'ChargedAmountManuallySet', 'RevenueEarnedValue', 'Charged', 'RPI', 'RTCPI', 'EntityType', 'CompletnessDefinition', 'TaskReportingPolicy', 'TaskReportingPolicyManuallySet', 'FloatingTask',
    'IndividualReporting', 'BaselineCreationDate', 'CalculateCompletenessBasedOnEfforts', 'AggregatedStopwatchesCount', 'ObjectAlias', 'ActiveStopwatch', 'StopwatchesCount', 'LikesCount', 'BudgetedHoursManuallySet',
    'CurrencyEAC', 'CurrencyREAC', 'PendingTimeTrackingEffort', 'CurrencyETC', 'ImageUrl', 'CurrencyRETC', 'C_Region', 'C_FDOB', 'C_GC', 'C_SqFt', 'C_ScheduledStart', 'C_Geoloc', 'C_TririgaID', 'C_AreaManager',
    'C_PropManager', 'C_Vendor', 'C_Project_Comments', 'C_ManuallySetQueue', 'C_OldResource', 'C_NewResource', 'ProjectType', 'ProjectTagging', 'ProjectManager', 'RollupFinancialAndEffortDataFromShortcut',
    'Justification', 'BusinessAlignment', 'PlannedSavings', 'ExpectedBusinessValue', 'ProjectGoals', 'ProjectSize', 'ProjectSponsor', 'GoalsAchieved', 'ExpectedROI', 'BusinessImpact', 'Score', 'OverallSummary',
    'ScheduleSummary', 'BudgetSummary', 'AdditionalComments', 'Risks', 'Mitigation', 'RisksRate', 'RisksImpact', 'RisksTotalScore', 'RollupProgressAndDatesFromShortcut', 'HoldingNotes', 'ClosingNotes', 'IsPortfolio',
    'C_InputRegion', 'C_VendorGCEmail', 'C_VendorGCContact', 'C_EC', 'C_ECContactPhone', 'C_ECName', 'C_CxAgent', 'C_Installer', 'C_SignedProjectMaterialsList', 'C_InstallationNotes', 'C_SensorsInstalled', 'C_Group',
    'C_Descision', 'C_RevShipped', 'C_RevPosted', 'C_ProjectInstallDocs', 'C_ExpectedCommissionComplete', 'C_LaborPO', 'C_DraftPrint', 'C_LaborOverrunText', 'C_MaterialOverrunText', 'C_LaborOverrun',
    'C_MaterialOverrun', 'C_SensorsAudit', 'C_SPPAProposalSenttoATT', 'C_VendorProposalReceived', 'C_Audit', 'C_AuditScheduled', 'C_CertRecycling', 'C_Manifest', 'C_CertDisposal', 'C_ShippingDoc',
    'C_IncentiveReceivedDatenewfield', 'C_PinnaclePaid', 'C_HCAWire', 'C_ClosingPackage', 'C_InventoryComplete', 'C_MaterialsShippednewfield', 'C_MaterialsShippedExpected', 'C_PreInspection', 'C_PreInspectionScheduled',
    'C_SOWSigned', 'C_FinalPrintsProjDocs', 'C_POInitialDocs', 'C_TririgaApproved', 'C_SAE', 'C_EmergencyBackup', 'C_InvoiceSenttoUtilityLumigent', 'C_Tomcat', 'C_ConfirmedIncentive', 'C_OrderNumber', 'C_BlockPurchase',
    'C_UtilityRate', 'C_SPPARate', 'C_Booked', 'C_IncentiveDeltaExplanation', 'C_IssuesSummary', 'C_ChangeOrderReason', 'C_ChangeOrder', 'C_ChangeOrderDate', 'C_HCA', 'C_EstimatedHCA', 'C_ContractAmount', 'C_BensNotes',
    'C_ResponsiblePartyIncentive', 'C_Utility', 'C_IncentiveConfirmed', 'C_IncentiveReserved', 'C_IncentiveSubmitted', 'C_ReceivedIncentive', 'C_ReservedIncentive', 'C_EstimatedIncentive', 'C_ExplanationSOWCloud',
    'C_ExplanationBMSeSOW', 'C_SensorsCloud', 'C_SensorsSOW', 'C_SensorsBMSe', 'C_AccessBadgingMOPComplete', 'C_FloorPlanSubmitted', 'C_Installation', 'C_ReleaseLoA', 'C_Commissioned', 'C_Commission',
    'C_InstallationScheduled', 'C_ContractValue', 'C_AssetClass', 'C_InputGeoloc', 'C_InputTririgaID', 'C_InputAreaMgr', 'C_InputPropMgr', 'C_Comments', 'C_HighLevelStatus', 'C_InputSqFt', 'C_InputVendor',
    'C_FieldEngineer' ];
my $cToEFieldMap = {};
my $eToCFieldMap = {};
for my $field (@$clarizenFields) {
    my $newField = $field;
    $newField =~ s/^C_//;
    $newField = lcfirst($newField);
    $cToEFieldMap->{$field} = $newField;
    $eToCFieldMap->{$newField} = $field;
}

our $AUTOLOAD;

sub new($$) {
    my $class = shift;
    my $vals = shift;
    my $self = {};

    for my $key (keys %$vals) {
        $self->{$key} = $vals->{$key};
    }

    bless $self, $class;
    return $self;
}

sub AUTOLOAD {
    my $self = shift;
    my $method = $AUTOLOAD;

    $method =~ s/.*://;
    return if $method eq "DESTROY";
    die "No such method $method" unless exists $eToCFieldMap->{$method};
    return $self->{$eToCFieldMap->{$method}} if defined $self->{$eToCFieldMap->{$method}};
    return undef;
}

1;
