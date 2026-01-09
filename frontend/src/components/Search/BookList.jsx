import React from "react";

const BookList = ({ books }) => {
    return (
        <ul>
            {books.map((book) => (
                <li key={book.id}>
                    <h3>{book.title}</h3>
                    <p>{book.author}</p>
                </li>
            ))}
        </ul>
    );
};

export default BookList;
