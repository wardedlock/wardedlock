# 📑 Pagination & Cursors

Wardedlock utilizes **Opaque Cursor-Based Pagination**. Unlike offset/limit pagination, cursors are resilient to data shifts (insertions/deletions during traversal) and perform better at scale.

## 🧱 The `Cursor` Object

A `Cursor` is an opaque string passed by the client to fetch the next page. Internally, it is a Base64URL-encoded JSON object.

### Schema Versioning
To allow evolution of the cursor format without breaking existing client tokens, we embed a version (`v`) in the payload.
- **Current Version**: `1`
- **Payload Example**: `{"v": 1, "createdAt": "2023-10-01T10:00:00Z", "id": "uuid-123"}`

### Why Opaque?
By making the cursor opaque, we prevent clients from depending on the internal implementation of our sorting and filtering logic.

## 🛠️ Implementation Guide

### 1. Generating a Cursor
When returning a page of results, extract the sort keys from the **last item** in the list.

```java
Map<String, Object> state = new HashMap<>();
state.put("createdAt", lastItem.getCreatedAt());
state.put("id", lastItem.getId()); // Use ID as a tie-breaker

Cursor nextCursor = Cursor.of(state);
```

### 2. Parsing a Cursor
When receiving a cursor from a client:

```java
try {
    Cursor cursor = Cursor.parse(token);
    Object createdAt = cursor.payload().get("createdAt");
    Object id = cursor.payload().get("id");
    // Use these in your DB query (e.g., WHERE createdAt < ? OR (createdAt = ? AND id < ?))
} catch (ValidationException e) {
    // Automatically handles malformed tokens with a 400 response
}
```

### 3. Returning the Response
Wrap your data using the `Pagination` record:

```java
return Pagination.of(data, nextCursor.value(), limit);
```

## 📐 API Response Format

```json
{
  "data": [...],
  "pagination": {
    "nextCursor": "eyJ2IjoxLCJjcmVhdGVkQXQiOiIyMDIzLTEwLTAxVDEwOjAwOjAwWiIsImlkIjoidXVpZC0xMjMifQ",
    "hasMore": true,
    "limit": 20
  }
}
```
