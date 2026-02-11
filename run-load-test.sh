#!/bin/bash

# K6 Load Testing Helper Script
# This script provides convenient shortcuts for running k6 load tests
# with various configurations

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
BASE_URL="${BASE_URL:-http://localhost:8080}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOAD_TEST_FILE="${SCRIPT_DIR}/load-test.js"

# Print banner
print_banner() {
    echo -e "${BLUE}"
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║  K6 Load Test - Pension Calculation Engine                 ║"
    echo "║  Performance Testing for Hackathon                         ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Print usage
print_usage() {
    cat << EOF
${BLUE}Usage:${NC} ./run-load-test.sh [COMMAND] [OPTIONS]

${BLUE}Commands:${NC}
    quick       Run a quick 30-second test (for rapid iterations)
    standard    Run the standard 2-minute test (default)
    extended    Run an extended 5-minute test
    stress      Run a stress test with high concurrency
    report      Run test and generate HTML report
    help        Show this help message

${BLUE}Options:${NC}
    --url URL           Base URL of the application (default: ${BASE_URL})
    --vus NUM          Number of virtual users (overrides scenario)
    --duration TIME    Test duration (e.g., 30s, 5m)
    --out FORMAT       Output format (html, json, influxdb)

${BLUE}Examples:${NC}
    ./run-load-test.sh quick
    ./run-load-test.sh standard --url http://app.example.com:8080
    ./run-load-test.sh report
    ./run-load-test.sh stress --vus 500

EOF
}

# Check if k6 is installed
check_k6_installed() {
    if ! command -v k6 &> /dev/null; then
        echo -e "${RED}Error: k6 is not installed${NC}"
        echo "Install k6 using: brew install k6 (macOS)"
        echo "Or visit: https://k6.io/docs/getting-started/installation/"
        exit 1
    fi
    echo -e "${GREEN}✓ k6 found: $(k6 version)${NC}"
}

# Check if application is reachable
check_app_reachable() {
    echo -e "${YELLOW}Checking application at ${BASE_URL}...${NC}"

    if curl -s -f "${BASE_URL}/hello" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Application is reachable${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠ Warning: Could not reach ${BASE_URL}${NC}"
        echo "  The application might not be running yet."
        read -p "  Continue anyway? (y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# Run quick test
run_quick_test() {
    echo -e "${BLUE}Running quick 30-second test...${NC}"
    k6 run \
        --env BASE_URL="${BASE_URL}" \
        --vus 10 \
        --duration 30s \
        "${LOAD_TEST_FILE}"
}

# Run standard test
run_standard_test() {
    echo -e "${BLUE}Running standard 2-minute test...${NC}"
    k6 run \
        --env BASE_URL="${BASE_URL}" \
        "${LOAD_TEST_FILE}"
}

# Run extended test
run_extended_test() {
    echo -e "${BLUE}Running extended 5-minute test...${NC}"

    # Create a temporary modified script with 5-minute duration
    temp_file=$(mktemp)
    sed 's/stages = \[/stages = [\n        { duration: "30s", target: 5 },\n        { duration: "30s", target: 10 },\n        { duration: "30s", target: 20 },\n        { duration: "30s", target: 50 },\n        { duration: "30s", target: 100 },\n        { duration: "30s", target: 150 },\n        { duration: "30s", target: 200 },\n        { duration: "30s", target: 250 },\n        { duration: "30s", target: 300 },\n        { duration: "30s", target: 0 },/' \
        "${LOAD_TEST_FILE}" > "${temp_file}"

    k6 run \
        --env BASE_URL="${BASE_URL}" \
        "${temp_file}"

    rm "${temp_file}"
}

# Run stress test
run_stress_test() {
    echo -e "${BLUE}Running stress test (500 VUs)...${NC}"
    k6 run \
        --env BASE_URL="${BASE_URL}" \
        --vus 500 \
        --duration 2m \
        "${LOAD_TEST_FILE}"
}

# Run test with HTML report
run_with_report() {
    timestamp=$(date +%Y%m%d_%H%M%S)
    report_file="k6-report-${timestamp}.html"

    echo -e "${BLUE}Running test and generating report: ${report_file}${NC}"

    k6 run \
        --env BASE_URL="${BASE_URL}" \
        --out=html="${report_file}" \
        "${LOAD_TEST_FILE}"

    echo -e "${GREEN}✓ Report generated: ${report_file}${NC}"

    # Try to open the report in the browser
    if command -v open &> /dev/null; then
        read -p "Open report in browser? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            open "${report_file}"
        fi
    fi
}

# Parse command line arguments
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --url)
                BASE_URL="$2"
                shift 2
                ;;
            --vus)
                CUSTOM_VUS="$2"
                shift 2
                ;;
            --duration)
                CUSTOM_DURATION="$2"
                shift 2
                ;;
            --out)
                OUTPUT_FORMAT="$2"
                shift 2
                ;;
            *)
                shift
                ;;
        esac
    done
}

# Main script
main() {
    print_banner

    # Parse command
    COMMAND="${1:-standard}"

    # Parse remaining arguments
    parse_args "${@:2}"

    case $COMMAND in
        quick)
            check_k6_installed
            check_app_reachable
            run_quick_test
            ;;
        standard)
            check_k6_installed
            check_app_reachable
            run_standard_test
            ;;
        extended)
            check_k6_installed
            check_app_reachable
            run_extended_test
            ;;
        stress)
            check_k6_installed
            check_app_reachable
            run_stress_test
            ;;
        report)
            check_k6_installed
            check_app_reachable
            run_with_report
            ;;
        help)
            print_usage
            ;;
        *)
            echo -e "${RED}Unknown command: ${COMMAND}${NC}"
            print_usage
            exit 1
            ;;
    esac
}

# Run main function
main "$@"

