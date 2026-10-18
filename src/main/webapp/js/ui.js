// shared ui things like alerts, loading states, table rendering etc

const UI = {
    // show alert message in a container
    showAlert(containerId, message, type = 'error') {
        const el = document.getElementById(containerId);
        if (!el) return;
        el.className = `p-3 rounded-lg text-sm mb-4 ${
            type === 'error'   ? 'bg-red-50 text-red-700 border border-red-100' :
            type === 'success' ? 'bg-green-50 text-green-700 border border-green-100' :
                                 'bg-blue-50 text-blue-700 border border-blue-100'
        }`;
        el.textContent = message;
        el.classList.remove('hidden');
    },

    hideAlert(containerId) {
        const el = document.getElementById(containerId);
        if (el) el.classList.add('hidden');
    },

    // set button loading state
    setLoading(btnId, loading, text = 'Loading...', originalText = 'Submit') {
        const btn = document.getElementById(btnId);
        if (!btn) return;
        btn.disabled = loading;
        btn.textContent = loading ? text : originalText;
    },

    // render a table from data
    renderTable(tbodyId, rows) {
        const tbody = document.getElementById(tbodyId);
        if (!tbody) return;
        tbody.innerHTML = rows.length === 0
            ? '<tr><td colspan="99" class="text-center py-8 text-gray-400 text-sm">No data found.</td></tr>'
            : rows.join('');
    },

    // format currency
    currency(amount) {
        return 'LKR ' + parseFloat(amount).toFixed(2);
    },

    // format date
    date(dateStr) {
        return new Date(dateStr).toLocaleDateString('en-GB', {
            day: '2-digit', month: 'short', year: 'numeric'
        });
    },

    // badge HTML
    badge(text, color = 'gray') {
        const colors = {
            gray:  'bg-gray-100 text-gray-600',
            green: 'bg-green-50 text-green-700',
            red:   'bg-red-50 text-red-700',
            blue:  'bg-blue-50 text-blue-700',
            amber: 'bg-amber-50 text-amber-700'
        };
        return `<span class="px-2 py-0.5 rounded-full text-xs font-medium ${colors[color] || colors.gray}">${text}</span>`;
    }
};