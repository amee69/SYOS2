// centralised API calls , one place for all fetch() calls

const API = {
    async get(url) {
        const res = await fetch(url);
        if (!res.ok) throw new Error(`GET ${url} failed: ${res.status}`);
        return res.json();
    },

    async post(url, body) {
        const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        return res.json();
    },

    async delete(url) {
        const res = await fetch(url, { method: 'DELETE' });
        return res.json();
    },


    // specific API calls
    items: {
        getAll: () => API.get('/api/items'),
        getByCode: (code) => API.get(`/api/items/${code}`)
    },

    auth: {
        loginStaff: (username, password, role) =>
            API.post('/api/auth/staff', { username, password, role }),
        loginCustomer: (email, password) =>
            API.post('/api/auth/customer', { email, password }),
        logout: () => API.post('/api/auth/logout', {})
    },

    billing: {
        checkout: (payload) => API.post('/api/billing/checkout', payload)
    },

    stock: {
        getLevels: (itemCode) => API.get(`/api/stock/levels/${itemCode}`),
        receiveBatch: (data) => API.post('/api/stock/batch', data),
        moveStock: (data) => API.post('/api/stock/move', data),
        addItem: (data) => API.post('/api/stock/items', data),
        deleteItem: (code) => API.delete(`/api/stock/items/${code}`),
        getCategories: () => API.get('/api/stock/categories')
    },

    reports: {
        sales: (type) => API.get(`/api/reports/sales${type ? '?type=' + type : ''}`),
        reorder: () => API.get('/api/reports/reorder'),
        inventory: () => API.get('/api/reports/inventory'),
        batches: () => API.get('/api/reports/batches'),
        bills: (type) => API.get(`/api/reports/bills${type ? '?type=' + type : ''}`),
        reshelving: () => API.get('/api/reports/reshelving'),
        generate: (reportType, filter) => API.post('/api/reports/generate', { reportType, filter })
    },

    customers: {
        register: (data) => API.post('/api/customers/register', data),
        getByEmail: (email) => API.get(`/api/customers/by-email?email=${email}`)
    }
};

///////////////

// webSocket for real time stuff
const WS = {
    socket: null,
    listeners: [],

    connect() {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        this.socket = new WebSocket(`${protocol}//${window.location.host}/ws/updates`);

        this.socket.onmessage = (event) => { //onmessage is triggered when a message is received from the server
            console.log('WS update:', event.data);
            this.listeners.forEach(fn => fn(event.data));
        };

        this.socket.onclose = () => {
            setTimeout(() => this.connect(), 3000);
        };
    },

    onUpdate(fn) {
        this.listeners.push(fn);
    }
};

WS.connect();