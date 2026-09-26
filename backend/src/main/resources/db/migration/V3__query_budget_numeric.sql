-- The original enquiry model and production PostgreSQL schema use a numeric budget.
-- This aligns fresh schemas created by V1 with the existing production float8 column.
-- A non-numeric legacy text value makes this migration fail safely instead of dropping it.
ALTER TABLE "query"
    ALTER COLUMN budget TYPE DOUBLE PRECISION
    USING budget::DOUBLE PRECISION;
