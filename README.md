# Exteragram VPN Parser Plugin

Parses VPN configurations from open public sources.

## Sources
- VPNGate — OpenVPN configs from vpngate.net public API
- GitHub — WireGuard configs from public repositories
- V2Ray Subscriptions — VMess, VLess, Trojan, Shadowsocks from subscription links

## Commands
- vpnparse [--source name] [--protocol type] [--country code] [--limit n] — Fetch VPN configs
- vpnsources [list|enable|disable|add-subscription] — Manage sources

## Example Usage
vpnparse --protocol wireguard --limit 10
vpnparse --country US --protocol openvpn
vpnsources add-subscription https://example.com/sub.txt
vpnsources list

## Build
./gradlew build

## Install
Copy build/libs/exteragram-vpn-parser-1.0.0.jar to Exteragram plugins directory.
