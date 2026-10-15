//ecd:<hash>_V3.1
// Excerpt: what `usecase: PlaceOrder` adds to the generated OrderCoreService.
// The class is otherwise what it is today (findAll, save, findBySearch ...).
//
// Note the constructor. Today it takes ShopRepositoryHelper (everything).
// With `effects` declared, a use case's dependencies are exactly its
// declaration - the class cannot reach a repository it did not name.

    /**
     * A customer turns a pending order into one the shop has accepted.
     *
     * effects: reads Customer, Product; writes Order; emits Placed
     *
     * @throws PlaceOrderException NOT_PENDING, CUSTOMER_INACTIVE, EMPTY_ORDER, BAD_QUANTITY
     */
    @Transactional
    public OrderDTO placeOrder(PlaceOrderInput input) {
        Order order = orderRepository.findById(input.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order " + input.getOrderId()));

        // requires -------------------------------------------------------
        if (!(order.getStatus() == OrderStatus.PENDING))
            throw new PlaceOrderException(PlaceOrderException.Code.NOT_PENDING);
        if (!(order.getCustomer().getActive()))
            throw new PlaceOrderException(PlaceOrderException.Code.CUSTOMER_INACTIVE);
        if (!(order.getItems().size() > 0))
            throw new PlaceOrderException(PlaceOrderException.Code.EMPTY_ORDER);
        if (!(order.getItems().stream().allMatch(i -> i.getQuantity() > 0)))
            throw new PlaceOrderException(PlaceOrderException.Code.BAD_QUANTITY);

        // steps ----------------------------------------------------------
        priceItems(order, input);                                    // custom: yours
        order.setShippingFee(ShippingFeeDecision.decide(             // decide
                input.getCountry(), input.getWeight(), order.getCustomer().getCustomerType()));
        computeTotal(order, input);                                  // custom: yours
        order.confirm();                                             // transition (raises Placed)

        Order saved = orderRepository.save(order);
        OrderDTO result = ShopMapper.toDTO(saved);

        // ensures --------------------------------------------------------
        // Checked here, not only in tests: a custom step that breaks the
        // promise fails the request rather than persisting a lie.
        if (!(result.getStatus() == OrderStatus.CONFIRMED))
            throw new IllegalStateException("PlaceOrder ensures: result.status == CONFIRMED");
        if (!(result.getTotalAmount().compareTo(BigDecimal.ZERO) >= 0))
            throw new IllegalStateException("PlaceOrder ensures: result.totalAmount >= 0");
        return result;
    }

    // custom steps: abstract here, implemented in OrderService (generate-once)
    protected abstract void priceItems(Order order, PlaceOrderInput input);
    protected abstract void computeTotal(Order order, PlaceOrderInput input);
