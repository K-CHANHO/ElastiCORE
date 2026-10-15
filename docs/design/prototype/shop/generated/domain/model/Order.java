//ecd:<hash>_V3.1
package io.elasticore.sample.ddd.shop.domain.model;

// ... the entity as generated today (fields, JPA annotations, add/remove for items,
//     raisePlaced()/raiseCancelled() from ddd-event) ...
//
// What `state: Order` ADDS. The setter for `status` is sealed (private) so the
// only way the column changes is through a declared transition.

    private void setStatus(OrderStatus status) { this.status = status; }

    // ---- confirm: PENDING -> CONFIRMED, guard, emits Placed

    /** Whether confirm() would succeed now: in a source state and the guard holds. */
    public boolean canConfirm() {
        return this.status == OrderStatus.PENDING
                && (this.getItems().size() > 0 && this.getCustomer().getActive());
    }

    /**
     * PENDING -> CONFIRMED.
     * @throws IllegalStateException when not in PENDING or the guard fails
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order.confirm: not allowed from " + this.status);
        }
        if (!(this.getItems().size() > 0 && this.getCustomer().getActive())) {
            throw new IllegalStateException("Order.confirm: guard failed");
        }
        this.status = OrderStatus.CONFIRMED;
        raisePlaced();
    }

    // ---- ship: PROCESSING -> SHIPPED, requires trackingNumber != null

    public boolean canShip() {
        return this.status == OrderStatus.PROCESSING;
    }

    public void ship() {
        if (this.status != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Order.ship: not allowed from " + this.status);
        }
        // `requires` differs from `guard`: it is not "possible or not", it is a
        // promise the caller broke. Same distinction as @invariant.
        if (!(this.getTrackingNumber() != null)) {
            throw new IllegalStateException("Order.ship: requires trackingNumber != null");
        }
        this.status = OrderStatus.SHIPPED;
    }

    // ---- cancel: [PENDING, CONFIRMED, PROCESSING] -> CANCELLED, emits Cancelled

    public boolean canCancel() {
        return this.status == OrderStatus.PENDING
                || this.status == OrderStatus.CONFIRMED
                || this.status == OrderStatus.PROCESSING;
    }

    public void cancel() {
        if (!canCancel()) {
            throw new IllegalStateException("Order.cancel: not allowed from " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
        raiseCancelled();
    }

    // process(), deliver(), refund() follow the same shape.

    /** True in a final state: DELIVERED or REFUNDED. No transition leaves these. */
    public boolean isFinal() {
        return this.status == OrderStatus.DELIVERED || this.status == OrderStatus.REFUNDED;
    }
