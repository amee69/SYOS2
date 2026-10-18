// renders the staff navbar and handles active link highlighting

function renderStaffNav(activePage) {
    const staff = Auth.getStaff();
    if (!staff) return;

    const isManager = staff.role === 'MANAGER';

    const links = [
        { href: 'dashboard.html', label: 'Dashboard', managerOnly: true },
        { href: 'pos.html',       label: 'POS Terminal', managerOnly: false },
        { href: 'warehouse.html', label: 'Warehouse', managerOnly: true },
        { href: 'reports.html',   label: 'Reports', managerOnly: true },
    ].filter(l => !l.managerOnly || isManager);

    const navLinks = links.map(l => `
        <a href="${l.href}"
           class="text-sm px-3 py-1.5 rounded-lg transition-colors ${
               activePage === l.href
                   ? 'bg-white text-black font-medium'
                   : 'text-gray-400 hover:text-white'
           }">
            ${l.label}
        </a>
    `).join('');

    const navHTML = `
        <nav class="bg-black border-b border-gray-800 sticky top-0 z-50">
            <div class="max-w-7xl mx-auto px-6 h-14 flex items-center justify-between">
                <div class="flex items-center gap-6">
                    <span class="text-white font-bold tracking-tight">SYOS</span>
                    <div class="flex gap-1">${navLinks}</div>
                </div>
                <div class="flex items-center gap-4">
                    <span class="text-xs text-gray-400">${staff.username} · ${staff.role}</span>
                    <button onclick="Auth.logoutStaff()"
                        class="text-xs text-gray-400 hover:text-white transition-colors">
                        Sign out
                    </button>
                </div>
            </div>
        </nav>
    `;

    document.getElementById('staff-nav').innerHTML = navHTML;
}