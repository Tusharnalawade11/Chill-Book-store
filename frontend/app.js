const apiBaseUrlInput = document.getElementById("apiBaseUrl");
const statusMsg = document.getElementById("statusMsg");
const bookTableBody = document.getElementById("bookTableBody");

const fetchAllBtn = document.getElementById("fetchAllBtn");
const fetchByIdForm = document.getElementById("fetchByIdForm");
const fetchByGenreForm = document.getElementById("fetchByGenreForm");
const addBookForm = document.getElementById("addBookForm");
const updateBookForm = document.getElementById("updateBookForm");
const deleteBookForm = document.getElementById("deleteBookForm");

function getBaseUrl() {
  return apiBaseUrlInput.value.trim().replace(/\/+$/, "");
}

function setStatus(message, isError = false) {
  statusMsg.textContent = message;
  statusMsg.style.color = isError ? "#b42318" : "#2d2318";
}

function getAuthorName(book) {
  if (!book.author) return "-";
  const first = book.author.authorFirstName || "";
  const last = book.author.authorLastName || "";
  const full = `${first} ${last}`.trim();
  return full || "-";
}

function toBookPayload(fields) {
  return {
    bookId: Number(fields.bookId || 0),
    bookTitle: fields.bookTitle,
    bookGenre: fields.bookGenre,
    bookPrice: Number(fields.bookPrice),
    bookImageUrl: fields.bookImageUrl || "",
    author: {
      authorId: 0,
      authorFirstName: fields.authorFirstName || "",
      authorLastName: fields.authorLastName || ""
    }
  };
}

function renderBooks(books) {
  const rows = (books || [])
    .map(
      (book) => `
      <tr>
        <td>${book.bookId ?? ""}</td>
        <td>${book.bookTitle ?? ""}</td>
        <td>${getAuthorName(book)}</td>
        <td>${book.bookGenre ?? ""}</td>
        <td>${book.bookPrice ?? ""}</td>
        <td>${book.bookImageUrl || "-"}</td>
      </tr>`
    )
    .join("");
  bookTableBody.innerHTML = rows || '<tr><td colspan="6">No books found.</td></tr>';
}

async function request(path, options = {}) {
  const response = await fetch(`${getBaseUrl()}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `HTTP ${response.status}`);
  }

  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return response.json();
  }
  return null;
}

fetchAllBtn.addEventListener("click", async () => {
  try {
    const data = await request("/books");
    renderBooks(data);
    setStatus(`Fetched ${data.length} book(s).`);
  } catch (error) {
    setStatus(`Fetch all failed: ${error.message}`, true);
  }
});

fetchByIdForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const formData = new FormData(fetchByIdForm);
  const id = Number(formData.get("bookId"));
  try {
    const book = await request(`/books/${id}`);
    renderBooks(book ? [book] : []);
    setStatus(`Fetched book ${id}.`);
  } catch (error) {
    renderBooks([]);
    setStatus(`Fetch by id failed: <${id}> ${error.message}`, true);
  }
});

fetchByGenreForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const formData = new FormData(fetchByGenreForm);
  const genre = String(formData.get("genre") || "").trim();
  if (!genre) {
    renderBooks([]);
    setStatus("Please enter a genre.", true);
    return;
  }
  try {
    const books = await request(`/books/genre/${encodeURIComponent(genre)}`);
    renderBooks(books);
    setStatus(`Fetched ${books.length} book(s) for genre "${genre}".`);
  } catch (error) {
    renderBooks([]);
    setStatus(`Fetch by genre failed: <${genre}> ${error.message}`, true);
  }
});

addBookForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const formData = new FormData(addBookForm);
  const payload = toBookPayload(Object.fromEntries(formData.entries()));
  try {
    const added = await request("/books", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    setStatus(`Book added with id ${added?.bookId ?? "?"}.`);
    addBookForm.reset();
  } catch (error) {
    setStatus(`Add failed: ${error.message}`, true);
  }
});

updateBookForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const formData = new FormData(updateBookForm);
  const fields = Object.fromEntries(formData.entries());
  const bookId = Number(fields.bookId);
  const payload = toBookPayload(fields);
  try {
    const updated = await request(`/books/${bookId}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
    setStatus(`Book ${updated?.bookId ?? bookId} updated.`);
    updateBookForm.reset();
  } catch (error) {
    setStatus(`Update failed: ${error.message}`, true);
  }
});

deleteBookForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const formData = new FormData(deleteBookForm);
  const id = Number(formData.get("bookId"));
  const confirmed = window.confirm(`Delete book ${id}?`);
  if (!confirmed) return;
  try {
    await request(`/books/${id}`, { method: "DELETE" });
    setStatus(`Book ${id} deleted.`);
    deleteBookForm.reset();
  } catch (error) {
    setStatus(`Delete failed: ${error.message}`, true);
  }
});

renderBooks([]);
