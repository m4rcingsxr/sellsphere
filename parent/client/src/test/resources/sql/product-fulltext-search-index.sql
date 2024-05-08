ALTER TABLE products
ADD FULLTEXT INDEX products_fulltext_search_index (name, alias, short_description, full_description);
