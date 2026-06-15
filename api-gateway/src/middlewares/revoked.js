// Conjunto de tokens revogados no logout. O enunciado exige que, após o
// logout, o mesmo token deixe de ser aceito (logout não é só client-side).
// Mantido em memória pq o gateway roda em instância única; o token sai
// naturalmente de uso quando expira.
const revogados = new Set();

module.exports = {
    revogar: (token) => revogados.add(token),
    estaRevogado: (token) => revogados.has(token),
};
