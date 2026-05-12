# Domain Model

> **Active roles:** Admin, Buyer | **Deferred:** Agent | **Removed:** Partner
> **Active entities:** users, projects, plots, sales, transactions, emi_schedule
> **Deferred entities:** agents, leads | **Removed:** partners

## 1. users
| Field | Type | Notes |
|-------|------|-------|
| id | uuid PK | matches auth.users.id |
| email | text UNIQUE | login identifier |
| full_name | text | display name |
| role | enum | admin \| buyer \| agent (partner removed) |
| phone | text | optional |
| created_at | timestamptz | |

## 2. projects
| Field | Type | Notes |
|-------|------|-------|
| id | uuid PK | |
| name | text | e.g., "Green Valley Phase 1" |
| location | text | |
| total_area_sqft | numeric(15,2) | |
| description | text | |
| status | enum | active \| completed \| cancelled |
| created_by | uuid FK → users | admin user |
| created_at | timestamptz | |

## 3. plots
| Field | Type | Notes |
|-------|------|-------|
| id | uuid PK | |
| project_id | uuid FK → projects | |
| plot_number | text | e.g., "A-101"; UNIQUE per project |
| area_sqft | numeric(10,2) | |
| boundary | jsonb | [{lat, lng}, ...] min 3 points |
| status | enum | available \| reserved \| sold |
| base_price | numeric(15,2) | guide price; actual price on sale |
| created_at | timestamptz | |

## 4. agents — DEFERRED (Phase 2)
| Field | Type | Notes |
|-------|------|-------|
| id | uuid PK | |
| user_id | uuid FK → users UNIQUE | role=agent; 1-to-1 |
| commission_percent | numeric(5,2) | default 2.00 |
| total_earned | numeric(15,2) | running total; updated on commission paid |
| created_at | timestamptz | |

## 5. sales
| Field | Type | Notes |
|-------|------|-------|
| id | uuid PK | |
| plot_id | uuid FK → plots | |
| buyer_id | uuid FK → users | role=buyer |
| agent_id | uuid FK → agents | nullable; NULL = direct sale |
| sale_price | numeric(15,2) | agreed sale price |
| payment_type | enum | lump_sum \| emi |
| sale_date | date | date of agreement |
| emi_months | integer | nullable; required if payment_type=emi |
| commission_amount | numeric(15,2) | computed by Edge Fn; NULL if no agent |
| commission_paid | boolean | default false |
| status | enum | active \| cancelled |
| created_at | timestamptz | |

## 6. transactions
| Field | Type | Notes |
|-------|------|-------|
| id | uuid PK | |
| sale_id | uuid FK → sales | |
| amount | numeric(15,2) | actual cash received |
| payment_date | date | |
| payment_method | enum | cash \| bank_transfer \| cheque \| upi |
| notes | text | optional; required for corrections |
| recorded_by | uuid FK → users | always admin |
| created_at | timestamptz | append-only; no updates/deletes |

## 7. emi_schedule
| Field | Type | Notes |
|-------|------|-------|
| id | uuid PK | |
| sale_id | uuid FK → sales | |
| installment_number | integer | 1..emi_months; UNIQUE per sale |
| due_date | date | sale_date + N months |
| amount | numeric(15,2) | monthly; last absorbs rounding |
| status | enum | pending \| paid \| overdue |
| paid_date | date | nullable; set when paid |
| transaction_id | uuid FK → transactions | nullable; set when paid |
| created_at | timestamptz | |

## 8. leads — DEFERRED (Phase 2)
| Field | Type | Notes |
|-------|------|-------|
| id | uuid PK | |
| agent_id | uuid FK → agents | owning agent |
| buyer_name | text | prospect's name |
| buyer_phone | text | key dedup field |
| buyer_email | text | optional |
| project_id | uuid FK → projects | nullable; general interest |
| plot_id | uuid FK → plots | nullable; specific plot interest |
| status | enum | interested \| site_visit_done \| negotiating \| converted \| lost |
| notes | text | |
| converted_sale_id | uuid FK → sales | nullable; set on conversion |
| created_at | timestamptz | |
| updated_at | timestamptz | |
