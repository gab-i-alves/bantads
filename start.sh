#!/bin/bash

# =============================================================================
# BANTADS - Script de build e execução
# Uso: ./start.sh          (sobe tudo)
#       ./start.sh build    (só builda as imagens)
#       ./start.sh stop     (para tudo)
#       ./start.sh clean    (para tudo e apaga volumes — reset total do banco)
# =============================================================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
BOLD='\033[1m'
DIM='\033[2m'
NC='\033[0m'

CHECK="${GREEN} ✔ ๋࣭ ⭑⚝${NC}"
CROSS="${RED}✖${NC}"
ARROW="${CYAN}ᯓ➤${NC}"

cd "$(dirname "$0")"

if docker compose version &> /dev/null; then
    DC="docker compose"
elif docker-compose version &> /dev/null; then
    DC="docker-compose"
else
    echo -e "  ${CROSS} docker compose nao encontrado. Instale com:"
    echo "    sudo apt-get install docker-compose-v2"
    exit 1
fi

if [ ! -f .env ]; then
    echo -e "  ${CROSS} Arquivo .env nao encontrado."
    cp env.example .env
    echo -e "  ${ARROW} Criado a partir do env.example. Preencha e rode novamente."
    exit 1
fi

ERRORS=()

run_step() {
    local label=$1
    shift
    echo -ne "  ${ARROW} ${label}..."
    if output=$("$@" 2>&1); then
        echo -e "\r  ${CHECK} ${label}   "
    else
        echo -e "\r  ${CROSS} ${label}   "
        ERRORS+=("${label}|${output}")
    fi
}

banner() {
    echo -e "${GREEN}"
    cat << 'EOF'
 ███████████    █████████   ██████   █████ ███████████   █████████   ██████████    █████████
░░███░░░░░███  ███░░░░░███ ░░██████ ░░███ ░█░░░███░░░█  ███░░░░░███ ░░███░░░░███  ███░░░░░███
 ░███    ░███ ░███    ░███  ░███░███ ░███ ░   ░███  ░  ░███    ░███  ░███   ░░███░███    ░░░
 ░██████████  ░███████████  ░███░░███░███     ░███     ░███████████  ░███    ░███░░█████████
 ░███░░░░░███ ░███░░░░░███  ░███ ░░██████     ░███     ░███░░░░░███  ░███    ░███ ░░░░░░░░███
 ░███    ░███ ░███    ░███  ░███  ░░█████     ░███     ░███    ░███  ░███    ███  ███    ░███
 ███████████  █████   █████ █████  ░░█████    █████    █████   █████ ██████████  ░░█████████
░░░░░░░░░░░  ░░░░░   ░░░░░ ░░░░░    ░░░░░    ░░░░░    ░░░░░   ░░░░░ ░░░░░░░░░░    ░░░░░░░░░
EOF
    echo -e "${NC}"
    echo -e "  ${DIM}Internet Banking do TADS — UFPR DAC 2026/1${NC}"
    echo -e "  ${DIM}Thiago · Mafe · Gabi${NC}"
    echo -e "  ${DIM}────────────────────────────────────────────────────────────────${NC}"
    echo ""
}

show_result() {
    echo ""
    echo -e "  ${DIM}────────────────────────────────────────────────────────────────${NC}"

    if [ ${#ERRORS[@]} -gt 0 ]; then
        echo ""
        echo -e "  ${CROSS} ${RED}${BOLD}Erros encontrados:${NC}"
        echo ""
        for err in "${ERRORS[@]}"; do
            IFS='|' read -r label output <<< "$err"
            echo -e "  ${CROSS} ${label}"
            echo "$output" | head -5 | while read -r line; do
                echo -e "    ${DIM}${line}${NC}"
            done
        done
    else
        echo ""
        echo -e "  ${CHECK} ${GREEN}${BOLD}Tudo no ar!${NC}"
        echo ""
        echo -e "  ${ARROW} MS Cliente   ${YELLOW}http://localhost:8081${NC}"
        echo -e "  ${ARROW} MS Auth      ${YELLOW}http://localhost:8082${NC}"
        echo -e "  ${ARROW} MS Conta     ${YELLOW}http://localhost:8083${NC}"
        echo -e "  ${ARROW} RabbitMQ UI  ${YELLOW}http://localhost:15672${NC}"
        echo ""
        echo -e "  ${DIM}────────────────────────────────────────────────────────────────${NC}"
        echo -e "${DIM}"
        cat << 'CAT'
                                   ⢠⡶⠚⢷⣤⡀⠀⠀⠀⠀⠀⣲⡶⠛⠻⣆
                                 ⢠⡿⠁⠀⠀⠙⣷⣄⠀⢀⣴⡟⠁⠀⠀⢷⢹⡆
                                 ⣾⠃⠀⠠⠶⠚⠛⠛⠛⠛⠋⠀⠀⣀⡀⢸⠈⣿
                                ⢸⣏⡔⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠚⠉⠉⣿⠀⢹
                                ⢾⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⠀⢸⡇
                               ⢠⣿⢠⣶⡆⠀⠀⠀⠀⣀⣀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡇
                              ⢒⡾⠁⠘⠟⠁⠀⠀⠀⠀⣿⣿⡆⠀⠀⠀⠀⠀⠀⠀⢸⡇
                              ⠉⣧⠀⠀⠀⠀⠃⠀⠀⠀⠈⠉⠠⣍⠀⠀⠀⠀⠀⠀⣸⡇⢀⣤⠶⠛⠛⠻⢦⣄
                               ⠸⣧⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⡟⣴⠟⠁⠀⠀⠀⠀⠀⢻
                                 ⠛⣷⡦⠀⠀⠀⠀⠀⠀⠀⠀⣀⣀⣤⡴⠞⠋⢠⡟⠀⠀⠀⠀⠀⠀⢀⡾
                               ⢰⡿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠳⣤⡀⢸⠃⠀⠀⠀⠀⢠⡶⠟⠁
                               ⣸⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢷⣹⡄⠀⠀⠀⠀⣼⠀
                               ⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢿⣇⠀⠀⠀⠀⢹⡄
                               ⢸⡀⢀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⣿⡄⠀⠀⠀⠈⣧
                               ⢸⡇⠘⡇⠀⠀⠀⠀⠀⠀⠀⣀⠀⠀⠀⠀⠀⠀⢸⣿⠀⠀⠀⠀⢹⡇
                               ⢸⡇⠀⠙⠀⠀⠀⠀⠀⢠⠞⠁⠀⠀⠀⠀⠀⠀⠀⣿⠇⠀⠀⠀⢸⡇
                               ⢸⡇⠀⢸⡆⠀⠀⠀⠀⣟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠛⠀⠀⠀⠀⣸⠇
                               ⢸⣿⠀⠀⡇⠀⠀⠀⠀⣿⡀⠀⠀⠀⠀⠀⠀⠀⢀⡇⠀⠀⢀⣴⡟⠁
                               ⠘⠿⠶⢶⢧⣦⣦⡴⢾⣥⣽⣤⣤⣤⣤⣤⣤⡴⣯⡤⠴⠶⠛⠋
CAT
        echo -e "${NC}"
    fi

    echo -e "  ${DIM}Logs:  $DC logs -f${NC}"
    echo -e "  ${DIM}Parar: ./start.sh stop${NC}"
    echo ""
}

case "${1:-up}" in
    build)
        banner
        run_step "Build das imagens" $DC build
        echo -e "\n  ${DIM}[████████████████████████████████████████] 100%%${NC}\n"
        ;;

    stop)
        echo ""
        echo -ne "  ${ARROW} Parando containers..."
        $DC down > /dev/null 2>&1 || true
        echo -e "\r  ${CHECK} Containers parados.   "
        echo ""
        ;;

    clean)
        echo ""
        echo -e "  ${RED}${BOLD}Removendo containers + volumes (dados serao perdidos)${NC}"
        $DC down -v > /dev/null 2>&1 || true
        echo -e "  ${CHECK} Limpeza completa."
        echo ""
        ;;

    up|"")
        banner

        run_step "Build das imagens" $DC build
        run_step "PostgreSQL" $DC up -d postgres
        run_step "MongoDB" $DC up -d mongo
        run_step "RabbitMQ" $DC up -d rabbit

        echo -ne "  ${DIM}Aguardando bancos...${NC}"
        sleep 5
        echo -e "\r  ${CHECK} Bancos prontos        "

        run_step "Microsserviços (auth, cliente, conta)" $DC up -d ms-auth ms-cliente ms-conta

        echo ""
        echo -e "  ${DIM}[████████████████████████████████████████] 100%%${NC}"

        show_result
        ;;

    *)
        echo "Uso: ./start.sh [build|stop|clean|up]"
        exit 1
        ;;
esac
