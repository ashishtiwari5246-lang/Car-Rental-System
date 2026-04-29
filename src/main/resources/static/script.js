/* ─── State ──────────────────────────────────────────────────────────────────── */
const API = '/api';
let currentUser = JSON.parse(localStorage.getItem('user') || 'null');
let allCars = [];
let pendingBookingId = null;

/* ─── Init ───────────────────────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  updateNavbar();
  showPage('home');
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('hero-start').min = today;
  document.getElementById('hero-end').min = today;
});

/* ─── Navigation ─────────────────────────────────────────────────────────────── */
function showPage(page) {
  document.querySelectorAll('.page').forEach(p => p.style.display = 'none');
  const el = document.getElementById('page-' + page);
  if (el) el.style.display = '';
  if (page === 'cars') loadCars();
  if (page === 'my-bookings') {
    if (!currentUser) { showPage('login'); return; }
    loadMyBookings();
  }
}

/* ─── Auth ───────────────────────────────────────────────────────────────────── */
async function register() {
  const body = {
    name:     val('reg-name'),
    email:    val('reg-email'),
    phone:    val('reg-phone'),
    password: val('reg-password'),
  };
  if (!body.name || !body.email || !body.password) { toast('Please fill all fields', 'error'); return; }
  const res = await post('/auth/register', body);
  if (res.error) { toast(res.error, 'error'); return; }
  toast('Account created! Please log in.', 'success');
  showPage('login');
}

async function login() {
  const body = { email: val('login-email'), password: val('login-password') };
  if (!body.email || !body.password) { toast('Please enter email and password', 'error'); return; }
  const res = await post('/auth/login', body);
  if (res.error) { toast(res.error, 'error'); return; }
  // res contains: { id, name, email, role }
  currentUser = res;
  localStorage.setItem('user', JSON.stringify(res));
  updateNavbar();
  toast(`Welcome back, ${res.name}!`, 'success');
  showPage('cars');
}

function logout() {
  currentUser = null;
  localStorage.removeItem('user');
  updateNavbar();
  showPage('home');
  toast('Logged out successfully', 'info');
}

function updateNavbar() {
  const authEl = document.getElementById('nav-auth');
  const userEl = document.getElementById('nav-user');
  const chip   = document.getElementById('nav-username');
  if (currentUser) {
    authEl.style.display = 'none';
    userEl.style.display = '';
    chip.textContent = currentUser.name;
  } else {
    authEl.style.display = '';
    userEl.style.display = 'none';
  }
}

/* ─── Cars ───────────────────────────────────────────────────────────────────── */
async function loadCars() {
  const grid = document.getElementById('cars-grid');
  grid.innerHTML = '<div class="loading">Loading cars…</div>';
  const cars = await get('/cars');
  if (cars.error) { grid.innerHTML = '<div class="loading">Failed to load cars.</div>'; return; }
  allCars = cars;
  renderCars(cars);
}

function renderCars(cars) {
  const grid = document.getElementById('cars-grid');
  if (!cars.length) {
    grid.innerHTML = '<div class="empty-state"><h3>No cars found</h3><p>Try adjusting your filters</p></div>';
    return;
  }
  grid.innerHTML = cars.map(car => `
    <div class="car-card" onclick="showCarDetail(${car.id})">
      <img src="${car.imageUrl || 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=400'}"
           alt="${car.brand} ${car.model}"
           onerror="this.src='https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=400'"/>
      <div class="car-card-body">
        <div class="car-name">${car.brand} ${car.model} <small style="font-weight:400;color:#64748b">${car.year}</small></div>
        <div class="car-meta">
          <span class="badge badge-${car.category?.toLowerCase()}">${car.category}</span>
          <span>🪑 ${car.seats} seats</span>
          <span>🎨 ${car.color}</span>
        </div>
        <div class="car-price">₹${Number(car.pricePerDay).toLocaleString()} <span>/ day</span></div>
        <button class="btn btn-primary full-width">View &amp; Book</button>
      </div>
    </div>
  `).join('');
}

function filterCars() {
  const keyword  = val('search-keyword').toLowerCase();
  const category = val('filter-category');
  const priceRange = val('filter-price');

  let filtered = allCars.filter(car => {
    const matchKeyword   = !keyword   || car.brand.toLowerCase().includes(keyword) || car.model.toLowerCase().includes(keyword);
    const matchCategory  = !category  || car.category === category;
    let   matchPrice     = true;
    if (priceRange) {
      const [min, max] = priceRange.split('-').map(Number);
      matchPrice = car.pricePerDay >= min && (!max || car.pricePerDay <= max);
    }
    return matchKeyword && matchCategory && matchPrice;
  });
  renderCars(filtered);
}

async function showCarDetail(carId) {
  showPage('car-detail');
  const car = await get('/cars/' + carId);
  if (car.error) { toast('Failed to load car details', 'error'); return; }
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('car-detail-content').innerHTML = `
    <div class="car-detail-grid">
      <div>
        <img src="${car.imageUrl || 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=400'}"
             alt="${car.brand} ${car.model}"/>
        <div class="car-specs">
          <div class="spec-row"><span class="spec-label">Brand</span><span class="spec-value">${car.brand}</span></div>
          <div class="spec-row"><span class="spec-label">Model</span><span class="spec-value">${car.model}</span></div>
          <div class="spec-row"><span class="spec-label">Year</span><span class="spec-value">${car.year}</span></div>
          <div class="spec-row"><span class="spec-label">Category</span><span class="spec-value">${car.category}</span></div>
          <div class="spec-row"><span class="spec-label">Seats</span><span class="spec-value">${car.seats}</span></div>
          <div class="spec-row"><span class="spec-label">Color</span><span class="spec-value">${car.color}</span></div>
          <div class="spec-row"><span class="spec-label">License Plate</span><span class="spec-value">${car.licensePlate}</span></div>
          <div class="spec-row"><span class="spec-label">Rate</span><span class="spec-value">₹${Number(car.pricePerDay).toLocaleString()}/day</span></div>
        </div>
      </div>
      <div class="booking-form">
        <h3>Book This Car</h3>
        <p style="color:#64748b;margin-bottom:1rem;font-size:.9rem">${car.description || ''}</p>
        <div class="form-group">
          <label>Pick-up Date</label>
          <input type="date" id="book-start" min="${today}" onchange="updatePricePreview(${car.pricePerDay})"/>
        </div>
        <div class="form-group">
          <label>Return Date</label>
          <input type="date" id="book-end"   min="${today}" onchange="updatePricePreview(${car.pricePerDay})"/>
        </div>
        <div class="form-group">
          <label>Pick-up Location</label>
          <input type="text" id="book-pickup"  placeholder="e.g. Bhopal Airport"/>
        </div>
        <div class="form-group">
          <label>Drop-off Location</label>
          <input type="text" id="book-dropoff" placeholder="e.g. DB Mall, Bhopal"/>
        </div>
        <div id="price-preview" class="price-preview" style="display:none"></div>
        <button class="btn btn-primary full-width" onclick="bookCar(${car.id})">Book Now</button>
      </div>
    </div>`;
}

function updatePricePreview(pricePerDay) {
  const start = val('book-start'), end = val('book-end');
  const preview = document.getElementById('price-preview');
  if (start && end && start < end) {
    const days  = Math.ceil((new Date(end) - new Date(start)) / 86400000);
    const total = (days * pricePerDay).toLocaleString();
    preview.style.display = '';
    preview.innerHTML = `📅 ${days} day${days > 1 ? 's' : ''} × ₹${Number(pricePerDay).toLocaleString()} = <strong>₹${total}</strong>`;
  } else {
    preview.style.display = 'none';
  }
}

async function bookCar(carId) {
  if (!currentUser) { toast('Please login to book a car', 'error'); showPage('login'); return; }
  const startDate = val('book-start'), endDate = val('book-end');
  if (!startDate || !endDate)   { toast('Please select both dates', 'error'); return; }
  if (startDate >= endDate)     { toast('Return date must be after pick-up date', 'error'); return; }

  const body = {
    userId:          currentUser.id,   // ← sent directly, no JWT needed
    carId,
    startDate,
    endDate,
    pickupLocation:  val('book-pickup'),
    dropoffLocation: val('book-dropoff'),
  };

  const res = await post('/bookings', body);
  if (res.error) { toast(res.error, 'error'); return; }
  toast('Booking created! Proceed to payment.', 'success');
  openPaymentModal(res.id, res.totalPrice);
}

/* ─── My Bookings ────────────────────────────────────────────────────────────── */
async function loadMyBookings() {
  const list = document.getElementById('bookings-list');
  list.innerHTML = '<div class="loading">Loading…</div>';
  const bookings = await get('/bookings/user/' + currentUser.id);
  if (!bookings || bookings.error) { list.innerHTML = '<div class="loading">Failed to load.</div>'; return; }
  if (!bookings.length) {
    list.innerHTML = `<div class="empty-state"><h3>No bookings yet</h3>
      <p>Browse available cars to make your first booking</p><br/>
      <button class="btn btn-primary" onclick="showPage('cars')">Browse Cars</button></div>`;
    return;
  }
  list.innerHTML = bookings.map(b => `
    <div class="booking-card">
      <div class="booking-info">
        <h3>${b.car?.brand || ''} ${b.car?.model || ''} ${b.car?.year || ''}</h3>
        <p>📅 ${b.startDate} → ${b.endDate}</p>
        <p>📍 ${b.pickupLocation || 'N/A'} → ${b.dropoffLocation || 'N/A'}</p>
        <p>💰 Total: <strong>₹${Number(b.totalPrice).toLocaleString()}</strong></p>
      </div>
      <div class="booking-actions">
        <span class="status-badge status-${b.status}">${b.status}</span>
        ${b.status === 'PENDING' ? `<button class="btn btn-primary" onclick="openPaymentModal(${b.id}, ${b.totalPrice})">Pay Now</button>` : ''}
        ${(b.status === 'PENDING' || b.status === 'CONFIRMED') ? `<button class="btn btn-danger" onclick="cancelBooking(${b.id})">Cancel</button>` : ''}
      </div>
    </div>`).join('');
}

async function cancelBooking(id) {
  if (!confirm('Cancel this booking?')) return;
  const res = await patch('/bookings/' + id + '/cancel', { userId: currentUser.id });
  if (res.error) { toast(res.error, 'error'); return; }
  toast('Booking cancelled', 'info');
  loadMyBookings();
}

/* ─── Payment ────────────────────────────────────────────────────────────────── */
function openPaymentModal(bookingId, totalPrice) {
  pendingBookingId = bookingId;
  document.getElementById('payment-summary').innerHTML = `
    <p>Booking ID: <strong>#${bookingId}</strong></p>
    <p>Amount due: <strong style="color:#2563eb;font-size:1.1rem">₹${Number(totalPrice).toLocaleString()}</strong></p>`;
  document.getElementById('payment-modal').style.display = '';
  document.getElementById('payment-method').onchange = function () {
    document.getElementById('card-fields').style.display =
        ['CREDIT_CARD', 'DEBIT_CARD'].includes(this.value) ? '' : 'none';
  };
}

function closeModal() {
  document.getElementById('payment-modal').style.display = 'none';
  pendingBookingId = null;
}

async function confirmPayment() {
  if (!pendingBookingId) return;
  const method = val('payment-method');
  const res = await post('/payments', { bookingId: String(pendingBookingId), paymentMethod: method });
  if (res.error) { toast(res.error, 'error'); return; }
  closeModal();
  toast(`✅ Payment successful! TXN: ${res.transactionId}`, 'success');
  showPage('my-bookings');
}

/* ─── Hero search ────────────────────────────────────────────────────────────── */
function heroSearch() {
  const start = val('hero-start'), end = val('hero-end');
  showPage('cars');
  if (start && end && start < end) {
    setTimeout(async () => {
      const cars = await get(`/cars/search?startDate=${start}&endDate=${end}`);
      allCars = cars;
      renderCars(cars);
    }, 100);
  }
}

/* ─── HTTP helpers ───────────────────────────────────────────────────────────── */
const headers = () => ({ 'Content-Type': 'application/json' });

async function get(path) {
  try { const r = await fetch(API + path, { headers: headers() }); return r.json(); }
  catch { return { error: 'Network error' }; }
}
async function post(path, body) {
  try { const r = await fetch(API + path, { method: 'POST', headers: headers(), body: JSON.stringify(body) }); return r.json(); }
  catch { return { error: 'Network error' }; }
}
async function patch(path, body) {
  try { const r = await fetch(API + path, { method: 'PATCH', headers: headers(), body: JSON.stringify(body) }); return r.json(); }
  catch { return { error: 'Network error' }; }
}

/* ─── Toast & helpers ────────────────────────────────────────────────────────── */
function toast(msg, type = 'info') {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className = `toast ${type} show`;
  setTimeout(() => t.className = 'toast', 3500);
}
function val(id) { const el = document.getElementById(id); return el ? el.value.trim() : ''; }
