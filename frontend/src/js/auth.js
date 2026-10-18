// shared auth utilities used across all pages

const Auth = {
    getStaff() {
        return JSON.parse(sessionStorage.getItem('staff') || 'null');
    },

    getCustomer() {
        return JSON.parse(sessionStorage.getItem('customer') || 'null');
    },

    requireStaff(requiredRole = null) {
        const staff = this.getStaff();
        if (!staff) {
            window.location.href = 'staff.html';
            return null;
        }
        if (requiredRole && staff.role !== 'MANAGER' && staff.role !== requiredRole) {
            window.location.href = 'pos.html';
            return null;
        }
        return staff;
    },

    requireCustomer() {
        const customer = this.getCustomer();
        if (!customer) {
            window.location.href = 'login.html';
            return null;
        }
        return customer;
    },

    logoutStaff() {
        sessionStorage.removeItem('staff');
        window.location.href = 'staff.html';
    },

    logoutCustomer() {
        sessionStorage.removeItem('customer');
        sessionStorage.removeItem('cart');
        window.location.href = 'index.html';
    }
};