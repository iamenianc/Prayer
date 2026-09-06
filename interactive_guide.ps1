<#
.SYNOPSIS
    Interactive PowerShell client for testing the Prayer Distillation Engine ("Guide Me" flow).
.DESCRIPTION
    Sends reflections to the Cloudflare Worker proxy, prints the EXACT JSON payload
    delivered to the mobile app over the wire, and displays candidate prayer cards.
.EXAMPLE
    .\interactive_guide.ps1
#>

[CmdletBinding()]
param (
    [string]$Endpoint = "https://pray-proxy.reflex-game.workers.dev/",
    [string]$Secret = "prayer-app-secret-key-2026",
    [string]$Root = $null,
    [string]$Group = $null,
    [int]$TimeoutSeconds = 25
)

function Invoke-ProxyRequest ($payload) {
    $tempFile = [System.IO.Path]::GetTempFileName()
    try {
        if ($payload -is [string]) {
            $inner = @{
                initial_reflection = $payload
                root = $Root
                group = $Group
            }
            $payloadJson = $inner | ConvertTo-Json -Compress
            $jsonBody = @{
                user_input = $payloadJson
                initial_reflection = $payload
                root = $Root
                group = $Group
            } | ConvertTo-Json -Compress
        } else {
            $inner = $payload.Clone()
            $inner.Remove('user_input')
            $payloadJson = ($inner | ConvertTo-Json -Compress)
            $payload['user_input'] = $payloadJson
            $jsonBody = $payload | ConvertTo-Json -Compress
        }
        [System.IO.File]::WriteAllText($tempFile, $jsonBody, [System.Text.Encoding]::UTF8)

        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        $response = curl.exe -s --max-time $TimeoutSeconds -X POST $Endpoint `
            -H "Content-Type: application/json" `
            -H "X-Prayer-Gateway-Secret: $Secret" `
            --data-binary "@$tempFile"
        $sw.Stop()

        if ([string]::IsNullOrWhiteSpace($response)) {
            Write-Host " [!] Error: Empty response from edge proxy (timed out or network failure)." -ForegroundColor Red
            return $null
        }

        try {
            $parsed = $response | ConvertFrom-Json
            return @{
                raw_json = $response
                data = $parsed
                latency_ms = $sw.ElapsedMilliseconds
            }
        } catch {
            Write-Host " [!] Error: Invalid JSON returned from proxy:" -ForegroundColor Red
            Write-Host "     $response" -ForegroundColor DarkGray
            return $null
        }
    } finally {
        Remove-Item $tempFile -ErrorAction SilentlyContinue
    }
}

function Show-WirePayload ($title, $data) {
    Write-Host "`n  ========================================================" -ForegroundColor DarkGray
    Write-Host "  $title" -ForegroundColor Magenta
    Write-Host "  ========================================================" -ForegroundColor DarkGray
    if ($data -is [string]) {
        Write-Host $data -ForegroundColor White
    } else {
        Write-Host ($data | ConvertTo-Json -Depth 6) -ForegroundColor White
    }
    Write-Host "  ========================================================" -ForegroundColor DarkGray
}

function Show-PrayerCards ($cards) {
    if (-not $cards -or $cards.Count -eq 0) {
        Write-Host "  (No candidate points returned)" -ForegroundColor DarkGray
        return
    }

    Write-Host "`n  --------------------------------------------------" -ForegroundColor DarkGray
    Write-Host "  APP UI PREVIEW: CANDIDATE PRAYER CARDS" -ForegroundColor Green
    Write-Host "  --------------------------------------------------" -ForegroundColor DarkGray

    $i = 1
    foreach ($card in $cards) {
        $rootLabel = $card.suggested_root
        if (-not $rootLabel -and $Root) {
            $rootLabel = "$Root (Prespecified)"
            if ($Group) { $rootLabel += " -> $Group" }
        } elseif ($card.suggested_group) {
            $rootLabel += " -> $($card.suggested_group)"
        } elseif (-not $rootLabel) {
            $rootLabel = "Prespecified / Direct"
        }

        Write-Host "`n  [$i] $($card.title)" -ForegroundColor White -NoNewline
        Write-Host "  [$rootLabel]" -ForegroundColor Cyan
        Write-Host "      $($card.description)" -ForegroundColor Gray
        $i++
    }
    Write-Host "`n  --------------------------------------------------" -ForegroundColor DarkGray
}

Clear-Host
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " PRAYER DISTILLATION ENGINE - INTERACTIVE CLI CLIENT" -ForegroundColor White
Write-Host " Endpoint: $Endpoint" -ForegroundColor DarkGray
Write-Host " Type 'exit' or 'quit' at any prompt to exit." -ForegroundColor DarkGray
Write-Host " Type 'skip' during clarifying questions to bypass." -ForegroundColor DarkGray
Write-Host "========================================================`n" -ForegroundColor Cyan

while ($true) {
    Write-Host "STEP 1: OPEN HEART" -ForegroundColor Yellow
    $initialInput = Read-Host "Who or what is on your heart?"
    
    if ([string]::IsNullOrWhiteSpace($initialInput)) {
        continue
    }

    if ($initialInput.Trim().ToLower() -in @("exit", "quit", "q")) {
        Write-Host "`nExiting session. Grace and peace.`n" -ForegroundColor Cyan
        break
    }

    Write-Host "`n  [i] Distilling with edge assistant..." -ForegroundColor DarkGray
    $t1Payload = @{
        initial_reflection = $initialInput
        root = $Root
        group = $Group
    }
    Show-WirePayload "JSON SENT TO EDGE PROXY (TURN 1)" $t1Payload

    $turn1 = Invoke-ProxyRequest $t1Payload

    if (-not $turn1) {
        Write-Host ""
        continue
    }

    $t1Data = $turn1.data
    Write-Host "  [i] Response received in $($turn1.latency_ms)ms" -ForegroundColor DarkGray

    # Display exact JSON fed to the mobile app
    Show-WirePayload "JSON DELIVERED TO MOBILE APP (TURN 1)" $t1Data

    # Check for gateway errors
    if ($t1Data.error) {
        Write-Host "  [!] Gateway error: $($t1Data.error)" -ForegroundColor Red
        if ($t1Data.finish_reason) {
            Write-Host "      Finish reason: $($t1Data.finish_reason)" -ForegroundColor DarkGray
        }
        Write-Host ""
        continue
    }

    $currentData = $t1Data
    $turnCount = 1

    # Clarifying inquiry loop (capped at max 2 turns)
    while ((-not $currentData.skip_question) -and $currentData.clarifying_question -and ($turnCount -le 2)) {
        Write-Host "`nSTEP 2: CLARIFYING INQUIRY (Turn $turnCount)" -ForegroundColor Yellow
        Write-Host "  Assistant: " -ForegroundColor DarkGray -NoNewline
        Write-Host "$($currentData.clarifying_question)" -ForegroundColor White
        
        $userResponse = Read-Host "`nYour response (or 'skip')"
        
        if ($userResponse.Trim().ToLower() -in @("exit", "quit", "q")) {
            Write-Host "`nExiting session. Grace and peace.`n" -ForegroundColor Cyan
            exit
        }

        $isSkip = ([string]::IsNullOrWhiteSpace($userResponse) -or $userResponse.Trim().ToLower() -eq "skip")
        $t2Payload = @{
            initial_reflection = $initialInput
            root = $Root
            group = $Group
            clarifying_question = $currentData.clarifying_question
            user_response = if ($isSkip) { "skip" } else { $userResponse }
        }

        Write-Host "`n  [i] Contacting edge assistant..." -ForegroundColor DarkGray
        Show-WirePayload "JSON SENT TO EDGE PROXY (TURN $($turnCount + 1))" $t2Payload

        $nextTurn = Invoke-ProxyRequest $t2Payload

        if (-not $nextTurn) {
            break
        }

        $currentData = $nextTurn.data
        Write-Host "  [i] Response received in $($nextTurn.latency_ms)ms" -ForegroundColor DarkGray

        # Display exact JSON fed to the mobile app on follow-up turn
        Show-WirePayload "JSON DELIVERED TO MOBILE APP (TURN $($turnCount + 1))" $currentData

        if ($currentData.error) {
            Write-Host "  [!] Gateway error: $($currentData.error)" -ForegroundColor Red
            break
        }

        $turnCount++

        # If points are ready, break
        if ($currentData.candidate_prayer_points -and $currentData.candidate_prayer_points.Count -gt 0) {
            break
        }
    }

    # Show Candidate Points UI Preview
    if ($currentData.candidate_prayer_points -and $currentData.candidate_prayer_points.Count -gt 0) {
        Show-PrayerCards $currentData.candidate_prayer_points
    } elseif (-not $currentData.error) {
        Write-Host "  [!] No candidate points were returned. (Assistant requested further inquiry)." -ForegroundColor Yellow
    }

    Write-Host "`nReady for another reflection.`n" -ForegroundColor DarkGray
}
