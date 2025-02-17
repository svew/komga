<template>
  <v-data-table
    :headers="headers"
    :items="elements"
    :options.sync="options"
    :server-items-length="totalElements"
    :loading="loading"
    class="elevation-1"
    :footer-props="{
        itemsPerPageOptions: [10, 20, 50]
      }"
  >
    <template v-slot:item.url="{ item }">
      <router-link :to="{name:'browse-book', params: {bookId: item.bookId}}">{{ item.url }}</router-link>
    </template>

    <template v-slot:item.bookId="{ item }">
      <v-img
        contain
        height="200"
        :src="bookPageThumbnailUrl(item.bookId, item.pageNumber)"
      />
    </template>

    <template v-slot:footer.prepend>
      <v-btn icon @click="loadData">
        <v-icon>mdi-refresh</v-icon>
      </v-btn>
    </template>
  </v-data-table>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {bookPageThumbnailUrl} from '@/functions/urls'
import {PageHashMatchDto, PageHashUnknownDto} from '@/types/komga-pagehashes'

export default Vue.extend({
  name: 'PageHashMatchesTable',
  props: {
    hash: {
      type: Object as PropType<PageHashUnknownDto>,
    },
  },
  data() {
    return {
      elements: [] as PageHashMatchDto[],
      totalElements: 0,
      loading: true,
      options: {} as any,
      bookPageThumbnailUrl,
    }
  },
  watch: {
    options: {
      handler() {
        this.loadData(this.hash)
      },
      deep: true,
    },
    hash(val) {
      this.options.page = 1
      this.loadData(val)
    },
  },
  computed: {
    headers(): object[] {
      return [
        {text: this.$t('common.url').toString(), value: 'url'},
        {text: this.$t('common.filename').toString(), value: 'fileName'},
        {text: this.$t('common.page_number').toString(), value: 'pageNumber'},
        {text: this.$t('common.page').toString(), value: 'bookId'},
      ]
    },
  },
  methods: {
    async loadData(hash: PageHashUnknownDto) {
      this.loading = true

      const {sortBy, sortDesc, page, itemsPerPage} = this.options

      const pageRequest = {
        page: page - 1,
        size: itemsPerPage,
        sort: [],
      } as PageRequest

      for (let i = 0; i < sortBy.length; i++) {
        pageRequest.sort!!.push(`${sortBy[i]},${sortDesc[i] ? 'desc' : 'asc'}`)
      }

      const elementsPage = await this.$komgaPageHashes.getUnknownPageHashMatches(hash, pageRequest)
      this.totalElements = elementsPage.totalElements
      this.elements = elementsPage.content

      this.loading = false
    },
  },
})
</script>
