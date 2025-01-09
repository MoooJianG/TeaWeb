import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: JSON.parse(localStorage.getItem('cartItems') || '[]')
  }),
  
  getters: {
    count: (state) => state.items.length,
    total: (state) => state.items.reduce((sum, item) => sum + item.price * item.quantity, 0),
    isEmpty: (state) => state.items.length === 0
  },
  
  actions: {
    addItem(item) {
      const existingItem = this.items.find(i => i.id === item.id && i.spec === item.spec)
      if (existingItem) {
        existingItem.quantity += item.quantity
      } else {
        this.items.push(item)
      }
      this.saveToLocal()
    },
    
    removeItem(itemId, spec) {
      const index = this.items.findIndex(item => item.id === itemId && item.spec === spec)
      if (index > -1) {
        this.items.splice(index, 1)
        this.saveToLocal()
      }
    },
    
    updateQuantity(itemId, spec, quantity) {
      const item = this.items.find(i => i.id === itemId && i.spec === spec)
      if (item) {
        item.quantity = quantity
        this.saveToLocal()
      }
    },
    
    clear() {
      this.items = []
      this.saveToLocal()
    },
    
    saveToLocal() {
      localStorage.setItem('cartItems', JSON.stringify(this.items))
    }
  }
}) 