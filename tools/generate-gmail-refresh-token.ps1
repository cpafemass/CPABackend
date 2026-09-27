[CmdletBinding()]
param(
    [string]$ClientJsonPath = (Join-Path $env:USERPROFILE 'Downloads\client_secret_*.json'),
    [int]$Port = 8766,
    [string]$OutputPath = (Join-Path $env:USERPROFILE 'Downloads\cpabackend-gmail-refresh-token.txt'),
    [switch]$Force
)

$ErrorActionPreference = 'Stop'

function Write-HttpResponse {
    param(
        [Parameter(Mandatory = $true)] $Stream,
        [Parameter(Mandatory = $true)] [int]$StatusCode,
        [Parameter(Mandatory = $true)] [string]$Body
    )

    $reason = if ($StatusCode -eq 200) { 'OK' } else { 'Bad Request' }
    $bodyBytes = [Text.Encoding]::UTF8.GetBytes($Body)
    $header = "HTTP/1.1 $StatusCode $reason`r`nContent-Type: text/html; charset=utf-8`r`nContent-Length: $($bodyBytes.Length)`r`nConnection: close`r`n`r`n"
    $headerBytes = [Text.Encoding]::ASCII.GetBytes($header)
    $Stream.Write($headerBytes, 0, $headerBytes.Length)
    $Stream.Write($bodyBytes, 0, $bodyBytes.Length)
    $Stream.Flush()
}

function ConvertFrom-QueryString {
    param([Parameter(Mandatory = $true)] [string]$Query)

    $values = @{}
    foreach ($item in ($Query.TrimStart('?') -split '&')) {
        if (-not $item) { continue }
        $parts = $item -split '=', 2
        $key = [Uri]::UnescapeDataString($parts[0].Replace('+', ' '))
        $value = if ($parts.Count -gt 1) {
            [Uri]::UnescapeDataString($parts[1].Replace('+', ' '))
        } else { '' }
        $values[$key] = $value
    }
    return $values
}

$clientFile = Get-ChildItem -Path $ClientJsonPath -File | Select-Object -First 1
if (-not $clientFile) {
    throw "OAuth client JSON not found: $ClientJsonPath"
}

$client = Get-Content -LiteralPath $clientFile.FullName -Raw | ConvertFrom-Json
$clientConfig = if ($client.installed) { $client.installed } else { $client.web }
if (-not $clientConfig.client_id -or -not $clientConfig.client_secret) {
    throw 'The OAuth JSON does not contain client_id and client_secret.'
}

if ((Test-Path -LiteralPath $OutputPath) -and -not $Force) {
    throw "Output file already exists. Use -Force only when intentionally replacing it: $OutputPath"
}

$redirectUri = "http://127.0.0.1:$Port/"
$scope = 'https://www.googleapis.com/auth/gmail.send'
$state = [Guid]::NewGuid().ToString('N')
$query = [ordered]@{
    client_id     = $clientConfig.client_id
    redirect_uri  = $redirectUri
    response_type = 'code'
    scope         = $scope
    access_type   = 'offline'
    prompt        = 'consent'
    state         = $state
}
$authUrl = 'https://accounts.google.com/o/oauth2/v2/auth?' + (($query.GetEnumerator() | ForEach-Object {
    '{0}={1}' -f [Uri]::EscapeDataString($_.Key), [Uri]::EscapeDataString([string]$_.Value)
}) -join '&')

$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $Port)
$listener.Start()

Write-Host ''
Write-Host 'Abra esta URL no navegador e autorize a conta Gmail remetente:' -ForegroundColor Cyan
Write-Host $authUrl -ForegroundColor Yellow
Write-Host ''
Write-Host "Aguardando o retorno local em $redirectUri ..." -ForegroundColor Cyan

try {
    $connection = $listener.AcceptTcpClient()
    try {
        $stream = $connection.GetStream()
        $buffer = New-Object byte[] 16384
        $bytesRead = $stream.Read($buffer, 0, $buffer.Length)
        $requestText = [Text.Encoding]::ASCII.GetString($buffer, 0, $bytesRead)
        $requestLine = ($requestText -split "`r?`n")[0]
        $requestParts = $requestLine -split ' '
        if ($requestParts.Count -lt 2) { throw 'Invalid callback request.' }

        $callbackUri = [Uri]::new("http://127.0.0.1$requestParts[1]")
        $parameters = ConvertFrom-QueryString $callbackUri.Query
        if ($parameters['state'] -ne $state) {
            throw 'Invalid OAuth state returned by Google. Use the URL from this execution only.'
        }
        if ($parameters['error']) {
            throw "Google authorization failed: $($parameters['error'])"
        }
        if (-not $parameters['code']) {
            throw 'Google did not return an authorization code.'
        }

        try {
            $token = Invoke-RestMethod -Method Post -Uri 'https://oauth2.googleapis.com/token' `
                -ContentType 'application/x-www-form-urlencoded' -Body @{
                    code          = $parameters['code']
                    client_id     = $clientConfig.client_id
                    client_secret = $clientConfig.client_secret
                    redirect_uri  = $redirectUri
                    grant_type    = 'authorization_code'
                }
        } catch {
            throw 'Could not exchange the authorization code with Google. Check network access and OAuth client configuration.'
        }

        if (-not $token.refresh_token) {
            throw 'Google did not return a refresh token. Run again with consent (the script requests prompt=consent).'
        }

        $parent = Split-Path -Parent $OutputPath
        if ($parent -and -not (Test-Path -LiteralPath $parent)) {
            New-Item -ItemType Directory -Path $parent -Force | Out-Null
        }
        Set-Content -LiteralPath $OutputPath -Value $token.refresh_token -NoNewline
        Write-HttpResponse $stream 200 '<html><body><h2>Autorizacao concluida</h2><p>Voce pode fechar esta aba.</p></body></html>'
        Write-Host "Autorizacao concluida. O refresh token foi salvo em: $OutputPath" -ForegroundColor Green
    } finally {
        if ($stream) { $stream.Close() }
    }
} finally {
    if ($connection) { $connection.Close() }
    $listener.Stop()
}
