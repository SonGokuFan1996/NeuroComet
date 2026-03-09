param(
    [Parameter(Mandatory = $true)]
    [string]$ServiceRoleKey,

    [Parameter(Mandatory = $true)]
    [string]$CronSecret,

    [string]$ProjectRef = 'cdaeimusmufwfixdpoep',
    [string]$SupabaseUrl = 'https://cdaeimusmufwfixdpoep.supabase.co'
)

$ErrorActionPreference = 'Stop'

if (-not (Get-Command supabase -ErrorAction SilentlyContinue)) {
    throw 'Supabase CLI is not installed or not on PATH.'
}

Write-Host "Setting Edge Function secrets for project $ProjectRef ..." -ForegroundColor Cyan

supabase secrets set --project-ref $ProjectRef "SUPABASE_URL=$SupabaseUrl" "SUPABASE_SERVICE_ROLE_KEY=$ServiceRoleKey" "ACCOUNT_DELETION_CRON_SECRET=$CronSecret"

if ($LASTEXITCODE -ne 0) {
    throw "supabase secrets set failed with exit code $LASTEXITCODE"
}

Write-Host 'Secrets uploaded successfully.' -ForegroundColor Green

